package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.LineupPlayerResponse;
import com.netballmanager.netballmanager.dto.LineupRequest;
import com.netballmanager.netballmanager.dto.LineupResponse;
import com.netballmanager.netballmanager.dto.LineupSelectionRequest;
import com.netballmanager.netballmanager.domain.Attendance;
import com.netballmanager.netballmanager.domain.AttendanceStatus;
import com.netballmanager.netballmanager.domain.AvailabilityStatus;
import com.netballmanager.netballmanager.domain.GameMatch;
import com.netballmanager.netballmanager.domain.LineupSelection;
import com.netballmanager.netballmanager.domain.MatchLineup;
import com.netballmanager.netballmanager.domain.MatchStatus;
import com.netballmanager.netballmanager.domain.MatchStatistics;
import com.netballmanager.netballmanager.domain.Player;
import com.netballmanager.netballmanager.domain.PlayerStatus;
import com.netballmanager.netballmanager.domain.Position;
import com.netballmanager.netballmanager.exception.BadRequestException;
import com.netballmanager.netballmanager.exception.NotFoundException;
import com.netballmanager.netballmanager.repository.AttendanceRepository;
import com.netballmanager.netballmanager.repository.GameMatchRepository;
import com.netballmanager.netballmanager.repository.LineupSelectionRepository;
import com.netballmanager.netballmanager.repository.MatchLineupRepository;
import com.netballmanager.netballmanager.repository.MatchStatisticsRepository;
import com.netballmanager.netballmanager.repository.PlayerAvailabilityRepository;
import com.netballmanager.netballmanager.repository.PlayerRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LineupService {
    private final MatchLineupRepository lineups;
    private final LineupSelectionRepository selections;
    private final GameMatchRepository matches;
    private final PlayerRepository players;
    private final PlayerAvailabilityRepository availability;
    private final AttendanceRepository attendance;
    private final MatchStatisticsRepository statistics;

    public LineupService(MatchLineupRepository lineups, LineupSelectionRepository selections,
                         GameMatchRepository matches, PlayerRepository players,
                         PlayerAvailabilityRepository availability, AttendanceRepository attendance,
                         MatchStatisticsRepository statistics) {
        this.lineups = lineups;
        this.selections = selections;
        this.matches = matches;
        this.players = players;
        this.availability = availability;
        this.attendance = attendance;
        this.statistics = statistics;
    }

    public LineupResponse save(Long matchId, LineupRequest request) {
        GameMatch match = requireMatch(matchId);
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new BadRequestException("A lineup can only be set for a scheduled match");
        }
        List<LineupSelectionRequest> starting = request.starting();
        List<LineupSelectionRequest> standby = request.standby() == null ? List.of() : request.standby();
        if (starting.size() != 7) {
            throw new BadRequestException("Exactly seven starting players are required");
        }
        if (standby.size() > 4) {
            throw new BadRequestException("At most four standby players are allowed");
        }
        Set<Position> assignedPositions = starting.stream().map(LineupSelectionRequest::position)
                .collect(Collectors.toSet());
        if (assignedPositions.size() != 7 || !assignedPositions.equals(EnumSet.allOf(Position.class))) {
            throw new BadRequestException("The starting seven must cover each court position exactly once");
        }
        Set<Long> selectedPlayerIds = new HashSet<>();
        List<LineupSelection> newSelections = new ArrayList<>();
        for (LineupSelectionRequest selection : starting) {
            newSelections.add(validateSelection(match, selection, selectedPlayerIds, true));
        }
        for (LineupSelectionRequest selection : standby) {
            newSelections.add(validateSelection(match, selection, selectedPlayerIds, false));
        }

        MatchLineup lineup = lineups.findByMatchId(matchId).orElseGet(() -> DomainFactory.createMatchLineup(match));
        lineup.clearSelections();
        lineups.saveAndFlush(lineup);
        lineup.replaceSelections(newSelections);
        return toResponse(lineups.saveAndFlush(lineup), "Lineup saved");
    }

    @Transactional(readOnly = true)
    public LineupResponse get(Long matchId) {
        return lineups.findByMatchId(matchId)
                .map(lineup -> toResponse(lineup, "Coach-selected lineup"))
                .orElseThrow(() -> new NotFoundException("No lineup exists for match " + matchId));
    }

    public LineupResponse recommend(Long matchId) {
        GameMatch match = requireMatch(matchId);
        List<Player> candidates = players.findAllByTeamIdAndStatus(match.getTeam().getId(), PlayerStatus.ACTIVE)
                .stream().filter(player -> availability.findByMatchIdAndPlayerId(matchId, player.getId())
                        .map(response -> response.getStatus() != AvailabilityStatus.UNAVAILABLE).orElse(true))
                .toList();
        if (candidates.size() < 7) {
            return new LineupResponse(matchId, List.of(), null,
                    "A valid lineup cannot be generated: fewer than seven active, non-unavailable players");
        }
        Map<Long, AvailabilityStatus> availabilityMap = availability.findAllByMatchId(matchId).stream()
                .collect(Collectors.toMap(response -> response.getPlayer().getId(),
                        response -> response.getStatus()));
        Map<Long, Integer> historyScores = candidates.stream().collect(Collectors.toMap(
                Player::getId, this::historyScore));
        List<Position> positions = List.of(Position.values());
        List<Player> bestPlayers = new ArrayList<>();
        int[] bestScore = {Integer.MIN_VALUE};
        recommendPosition(0, positions, candidates, availabilityMap, historyScores, new ArrayList<>(),
                new HashSet<>(), 0, bestScore, bestPlayers);
        if (bestPlayers.size() != 7) {
            return new LineupResponse(matchId, List.of(), null,
                    "A valid lineup cannot be generated because the available squad cannot cover all seven positions");
        }
        List<LineupPlayerResponse> suggestions = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            Player player = bestPlayers.get(i);
            suggestions.add(new LineupPlayerResponse(player.getId(), fullName(player), positions.get(i), true));
        }
        List<Player> bench = candidates.stream().filter(player -> !bestPlayers.contains(player))
                .sorted(Comparator.comparingInt((Player player) -> benchScore(player, historyScores)).reversed())
                .limit(4).toList();
        for (Player player : bench) {
            Position pos = player.getPreferredPositions().stream().findFirst().orElse(Position.C);
            suggestions.add(new LineupPlayerResponse(player.getId(), fullName(player), pos, false));
        }
        return new LineupResponse(matchId, suggestions, null,
                "Advisory suggestion; the coach may edit and save this lineup");
    }

    private void recommendPosition(int index, List<Position> positions, List<Player> candidates,
                                   Map<Long, AvailabilityStatus> availabilityMap, Map<Long, Integer> historyScores,
                                   List<Player> current,
                                   Set<Long> used, int score, int[] bestScore, List<Player> bestPlayers) {
        if (index == positions.size()) {
            if (score > bestScore[0]) {
                bestScore[0] = score;
                bestPlayers.clear();
                bestPlayers.addAll(current);
            }
            return;
        }
        Position position = positions.get(index);
        for (Player player : candidates) {
            if (used.contains(player.getId())) {
                continue;
            }
            int positionScore = player.getPreferredPositions().contains(position) ? 100 : 0;
            AvailabilityStatus status = availabilityMap.get(player.getId());
            positionScore += status == AvailabilityStatus.AVAILABLE ? 30
                    : status == AvailabilityStatus.MAYBE ? 10 : 0;
            positionScore += historyScores.get(player.getId());
            current.add(player);
            used.add(player.getId());
            recommendPosition(index + 1, positions, candidates, availabilityMap, historyScores, current, used,
                    score + positionScore, bestScore, bestPlayers);
            current.remove(current.size() - 1);
            used.remove(player.getId());
        }
    }

    private int historyScore(Player player) {
        List<Attendance> history = attendance.findAllByPlayerId(player.getId());
        long attended = history.stream().filter(item -> item.getStatus() == AttendanceStatus.PRESENT
                || item.getStatus() == AttendanceStatus.LATE).count();
        long attendedSessions = history.stream()
                .filter(item -> item.getStatus() != AttendanceStatus.EXCUSED).count();
        int attendanceScore = attendedSessions == 0 ? 0 : (int) (20 * attended / attendedSessions);
        List<MatchStatistics> recent = statistics.findAllByPlayerIdOrderByMatchMatchDateTimeDesc(player.getId());
        int performance = recent.stream().limit(3)
                .mapToInt(item -> item.getGoalsScored() + item.getAssists() + item.getInterceptions()
                        + item.getDeflections() + item.getRebounds() - item.getTurnovers())
                .sum();
        long previousSelections = selections.countByPlayerIdAndStartingTrue(player.getId());
        return attendanceScore + Math.min(performance, 30) - (int) Math.min(previousSelections, 20);
    }

    private int benchScore(Player player, Map<Long, Integer> historyScores) {
        return player.getPreferredPositions().size() * 2 + historyScores.get(player.getId());
    }

    private LineupSelection validateSelection(GameMatch match, LineupSelectionRequest request,
                                              Set<Long> selectedIds, boolean starting) {
        if (!selectedIds.add(request.playerId())) {
            throw new BadRequestException("A player cannot be assigned more than once in a lineup");
        }
        Player player = players.findByIdAndTeamId(request.playerId(), match.getTeam().getId())
                .orElseThrow(() -> new BadRequestException("Every selected player must belong to the match team"));
        if (player.getStatus() != PlayerStatus.ACTIVE) {
            throw new BadRequestException("Inactive players cannot be selected");
        }
        if (availability.findByMatchIdAndPlayerId(match.getId(), player.getId())
                .map(response -> response.getStatus() == AvailabilityStatus.UNAVAILABLE).orElse(false)) {
            throw new BadRequestException("Unavailable players cannot be selected");
        }
        return DomainFactory.createLineupSelection(player, request.position(), starting);
    }

    private GameMatch requireMatch(Long matchId) {
        return matches.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match " + matchId + " was not found"));
    }

    private static LineupResponse toResponse(MatchLineup lineup, String message) {
        List<LineupPlayerResponse> players = lineup.getSelections().stream()
                .sorted(Comparator.comparing(LineupSelection::isStarting).reversed()
                        .thenComparing(selection -> selection.getPosition().ordinal()))
                .map(selection -> new LineupPlayerResponse(selection.getPlayer().getId(),
                        fullName(selection.getPlayer()), selection.getPosition(), selection.isStarting()))
                .toList();
        return new LineupResponse(lineup.getMatch().getId(), players, lineup.getUpdatedAt(), message);
    }

    private static String fullName(Player player) {
        return player.getFirstName() + " " + player.getLastName();
    }
}
