package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.SubstitutionRequest;
import com.netballmanager.netballmanager.dto.SubstitutionResponse;
import com.netballmanager.netballmanager.domain.AvailabilityStatus;
import com.netballmanager.netballmanager.domain.GameMatch;
import com.netballmanager.netballmanager.domain.LineupSelection;
import com.netballmanager.netballmanager.domain.MatchLineup;
import com.netballmanager.netballmanager.domain.MatchStatus;
import com.netballmanager.netballmanager.domain.Player;
import com.netballmanager.netballmanager.domain.PlayerStatus;
import com.netballmanager.netballmanager.domain.Substitution;
import com.netballmanager.netballmanager.exception.BadRequestException;
import com.netballmanager.netballmanager.exception.NotFoundException;
import com.netballmanager.netballmanager.repository.MatchLineupRepository;
import com.netballmanager.netballmanager.repository.PlayerAvailabilityRepository;
import com.netballmanager.netballmanager.repository.SubstitutionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubstitutionService {
    private final SubstitutionRepository substitutions;
    private final MatchLineupRepository lineups;
    private final PlayerAvailabilityRepository availability;
    private final GameMatchService matches;
    private final PlayerService players;

    public SubstitutionService(SubstitutionRepository substitutions, MatchLineupRepository lineups,
                               PlayerAvailabilityRepository availability, GameMatchService matches,
                               PlayerService players) {
        this.substitutions = substitutions;
        this.lineups = lineups;
        this.availability = availability;
        this.matches = matches;
        this.players = players;
    }

    public SubstitutionResponse record(Long matchId, SubstitutionRequest request) {
        GameMatch match = matches.require(matchId);
        if (match.getStatus() == MatchStatus.CANCELLED) {
            throw new BadRequestException("Cannot substitute players in a cancelled match");
        }
        if (request.playerOutId().equals(request.playerInId())) {
            throw new BadRequestException("A substitution requires two different players");
        }
        MatchLineup lineup = lineups.findByMatchId(matchId)
                .orElseThrow(() -> new NotFoundException("No lineup exists for match " + matchId));
        LineupSelection outgoing = lineup.getSelections().stream()
                .filter(selection -> selection.getPlayer().getId().equals(request.playerOutId())
                        && selection.isStarting())
                .findFirst().orElseThrow(() -> new BadRequestException("Player substituted out must be in the starting seven"));
        LineupSelection incoming = lineup.getSelections().stream()
                .filter(selection -> selection.getPlayer().getId().equals(request.playerInId())
                        && !selection.isStarting())
                .findFirst().orElseThrow(() -> new BadRequestException("Player substituted in must be a selected standby player"));
        if (outgoing.getPosition() != request.position()) {
            throw new BadRequestException("The substituted position does not match the starting lineup");
        }
        Player in = incoming.getPlayer();
        if (in.getStatus() != PlayerStatus.ACTIVE
                || availability.findByMatchIdAndPlayerId(matchId, in.getId())
                .map(response -> response.getStatus() == AvailabilityStatus.UNAVAILABLE).orElse(false)) {
            throw new BadRequestException("Inactive or unavailable players cannot be substituted in");
        }
        outgoing.setStarting(false);
        incoming.setStarting(true);
        incoming.setPosition(request.position());
        lineup.touch();
        lineup.getSelections().stream().filter(selection -> selection.isStarting()
                        && selection != incoming && selection.getPosition() == request.position())
                .forEach(selection -> selection.setStarting(false));
        return toResponse(substitutions.save(DomainFactory.createSubstitution(match, outgoing.getPlayer(), in,
                outgoing.getPosition(), request.position(), request.period())));
    }

    @Transactional(readOnly = true)
    public List<SubstitutionResponse> list(Long matchId) {
        matches.require(matchId);
        return substitutions.findAllByMatchIdOrderBySubstitutedAt(matchId).stream()
                .map(SubstitutionService::toResponse).toList();
    }

    private static SubstitutionResponse toResponse(Substitution item) {
        return new SubstitutionResponse(item.getId(), item.getMatch().getId(), item.getPlayerOut().getId(),
                fullName(item.getPlayerOut()), item.getPlayerIn().getId(), fullName(item.getPlayerIn()),
                item.getPreviousPosition(), item.getNewPosition(), item.getPeriod(), item.getSubstitutedAt());
    }

    private static String fullName(Player player) {
        return player.getFirstName() + " " + player.getLastName();
    }
}
