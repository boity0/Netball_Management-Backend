package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.MatchRequest;
import com.netballmanager.netballmanager.dto.MatchResponse;
import com.netballmanager.netballmanager.domain.GameMatch;
import com.netballmanager.netballmanager.domain.MatchStatus;
import com.netballmanager.netballmanager.exception.BadRequestException;
import com.netballmanager.netballmanager.exception.NotFoundException;
import com.netballmanager.netballmanager.repository.GameMatchRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GameMatchService {
    private final GameMatchRepository matches;
    private final TeamService teams;

    public GameMatchService(GameMatchRepository matches, TeamService teams) {
        this.matches = matches;
        this.teams = teams;
    }

    public MatchResponse create(Long teamId, MatchRequest request) {
        validateFuture(request.matchDateTime());
        return toResponse(matches.save(DomainFactory.createMatch(teams.require(teamId), request.opponentName().trim(),
                request.venue().trim(), request.matchDateTime(), request.venueType())));
    }

    public MatchResponse update(Long id, MatchRequest request) {
        GameMatch match = require(id);
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled matches can be edited");
        }
        validateFuture(request.matchDateTime());
        match.setOpponentName(request.opponentName().trim());
        match.setVenue(request.venue().trim());
        match.setMatchDateTime(request.matchDateTime());
        match.setVenueType(request.venueType());
        return toResponse(match);
    }

    public MatchResponse cancel(Long id) {
        GameMatch match = require(id);
        match.setStatus(MatchStatus.CANCELLED);
        return toResponse(match);
    }

    public MatchResponse recordResult(Long id, int teamScore, int opponentScore) {
        GameMatch match = require(id);
        if (match.getStatus() == MatchStatus.CANCELLED) {
            throw new BadRequestException("A cancelled match cannot have a result");
        }
        match.setResult(teamScore, opponentScore);
        return toResponse(match);
    }

    @Transactional(readOnly = true)
    public MatchResponse get(Long id) {
        return toResponse(require(id));
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> list(Long teamId, MatchStatus status) {
        teams.require(teamId);
        List<GameMatch> result = status == MatchStatus.SCHEDULED
                ? matches.findAllByTeamIdAndStatusAndMatchDateTimeAfterOrderByMatchDateTime(
                        teamId, status, LocalDateTime.now())
                : status == null ? matches.findAllByTeamIdOrderByMatchDateTime(teamId)
                : matches.findAllByTeamIdAndStatusOrderByMatchDateTime(teamId, status);
        return result.stream().map(GameMatchService::toResponse).toList();
    }

    public GameMatch require(Long id) {
        return matches.findById(id)
                .orElseThrow(() -> new NotFoundException("Match " + id + " was not found"));
    }

    private static void validateFuture(LocalDateTime dateTime) {
        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Match date and time must be in the future");
        }
    }

    private static MatchResponse toResponse(GameMatch match) {
        return new MatchResponse(match.getId(), match.getTeam().getId(), match.getOpponentName(), match.getVenue(),
                match.getMatchDateTime(), match.getVenueType(), match.getStatus(),
                match.getTeamScore(), match.getOpponentScore());
    }
}
