package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.AvailabilityRequest;
import com.netballmanager.netballmanager.dto.AvailabilitySummary;
import com.netballmanager.netballmanager.dto.PlayerResponse;
import com.netballmanager.netballmanager.domain.AvailabilityStatus;
import com.netballmanager.netballmanager.domain.GameMatch;
import com.netballmanager.netballmanager.domain.MatchStatus;
import com.netballmanager.netballmanager.domain.Player;
import com.netballmanager.netballmanager.domain.PlayerAvailability;
import com.netballmanager.netballmanager.domain.PlayerStatus;
import com.netballmanager.netballmanager.exception.BadRequestException;
import com.netballmanager.netballmanager.exception.NotFoundException;
import com.netballmanager.netballmanager.repository.PlayerAvailabilityRepository;
import com.netballmanager.netballmanager.repository.PlayerRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AvailabilityService {
    private final PlayerAvailabilityRepository availability;
    private final PlayerRepository players;
    private final PlayerService playerService;
    private final GameMatchService matchService;

    public AvailabilityService(PlayerAvailabilityRepository availability, PlayerRepository players,
                               PlayerService playerService, GameMatchService matchService) {
        this.availability = availability;
        this.players = players;
        this.playerService = playerService;
        this.matchService = matchService;
    }

    public AvailabilitySummary respond(Long matchId, AvailabilityRequest request) {
        GameMatch match = matchService.require(matchId);
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new BadRequestException("Availability can only be updated for scheduled matches");
        }
        Player player = playerService.require(request.playerId());
        if (!player.getTeam().getId().equals(match.getTeam().getId())) {
            throw new BadRequestException("Player is not registered with the match team");
        }
        if (player.getStatus() != PlayerStatus.ACTIVE) {
            throw new BadRequestException("Inactive players cannot update match availability");
        }
        PlayerAvailability response = availability.findByMatchIdAndPlayerId(matchId, player.getId())
                .orElseGet(() -> DomainFactory.createAvailability(match, player, request.status()));
        response.setStatus(request.status());
        availability.save(response);
        return summary(matchId);
    }

    @Transactional(readOnly = true)
    public AvailabilitySummary summary(Long matchId) {
        GameMatch match = matchService.require(matchId);
        List<Player> squad = players.findAllByTeamIdAndStatus(match.getTeam().getId(), PlayerStatus.ACTIVE);
        Set<Long> activePlayerIds = squad.stream().map(Player::getId).collect(Collectors.toSet());
        Map<Long, AvailabilityStatus> responses = availability.findAllByMatchId(matchId).stream()
                .filter(item -> activePlayerIds.contains(item.getPlayer().getId()))
                .collect(Collectors.toMap(item -> item.getPlayer().getId(), PlayerAvailability::getStatus));
        List<PlayerResponse> unavailable = squad.stream()
                .filter(player -> responses.get(player.getId()) == AvailabilityStatus.UNAVAILABLE)
                .map(AvailabilityService::toResponse).toList();
        List<PlayerResponse> noResponse = squad.stream()
                .filter(player -> !responses.containsKey(player.getId()))
                .map(AvailabilityService::toResponse).toList();
        int availableCount = (int) squad.stream()
                .filter(player -> responses.get(player.getId()) == AvailabilityStatus.AVAILABLE).count();
        String message = availableCount < 7
                ? "Fewer than seven players are available; the coach should review the squad"
                : "Sufficient players are available";
        return new AvailabilitySummary(availableCount, unavailable, noResponse, responses, message);
    }

    public PlayerAvailability requireResponse(Long matchId, Long playerId) {
        return availability.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow(() -> new NotFoundException("No availability response was found"));
    }

    private static PlayerResponse toResponse(Player player) {
        return new PlayerResponse(player.getId(), player.getTeam().getId(), player.getFirstName(),
                player.getLastName(), player.getEmail(), player.getPhoneNumber(), player.getJerseyNumber(),
                player.getPreferredPositions(), player.getStatus(), player.getRegistrationDate());
    }
}
