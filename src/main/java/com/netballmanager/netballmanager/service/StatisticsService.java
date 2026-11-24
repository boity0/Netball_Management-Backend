package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.StatisticsRequest;
import com.netballmanager.netballmanager.dto.StatisticsResponse;
import com.netballmanager.netballmanager.domain.GameMatch;
import com.netballmanager.netballmanager.domain.MatchStatistics;
import com.netballmanager.netballmanager.domain.Player;
import com.netballmanager.netballmanager.exception.BadRequestException;
import com.netballmanager.netballmanager.repository.MatchStatisticsRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StatisticsService {
    private final MatchStatisticsRepository statistics;
    private final GameMatchService matches;
    private final PlayerService players;

    public StatisticsService(MatchStatisticsRepository statistics, GameMatchService matches,
                             PlayerService players) {
        this.statistics = statistics;
        this.matches = matches;
        this.players = players;
    }

    public StatisticsResponse record(Long matchId, StatisticsRequest request) {
        GameMatch match = matches.require(matchId);
        Player player = players.require(request.playerId());
        if (!player.getTeam().getId().equals(match.getTeam().getId())) {
            throw new BadRequestException("Player is not registered with this match team");
        }
        if (request.goalsScored() > request.goalAttempts()) {
            throw new BadRequestException("Goals scored cannot exceed goal attempts");
        }
        if (statistics.findByMatchIdAndPlayerId(matchId, player.getId()).isPresent()) {
            throw new BadRequestException("Statistics for this player and match have already been recorded");
        }
        MatchStatistics result = statistics.save(DomainFactory.createStatistics(match, player, request.goalsScored(),
                request.goalAttempts(), request.assists(), request.interceptions(), request.deflections(),
                request.rebounds(), request.turnovers(), request.penalties(), request.minutesPlayed()));
        return toResponse(result);
    }

    @Transactional(readOnly = true)
    public List<StatisticsResponse> history(Long playerId) {
        players.require(playerId);
        return statistics.findAllByPlayerIdOrderByMatchMatchDateTimeDesc(playerId).stream()
                .map(StatisticsService::toResponse).toList();
    }

    private static StatisticsResponse toResponse(MatchStatistics stats) {
        Player player = stats.getPlayer();
        return new StatisticsResponse(stats.getMatch().getId(), player.getId(),
                player.getFirstName() + " " + player.getLastName(), stats.getGoalsScored(),
                stats.getGoalAttempts(), stats.getAssists(), stats.getInterceptions(), stats.getDeflections(),
                stats.getRebounds(), stats.getTurnovers(), stats.getPenalties(), stats.getMinutesPlayed());
    }
}
