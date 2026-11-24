package com.netballmanager.netballmanager.controller;

import com.netballmanager.netballmanager.dto.MatchRequest;
import com.netballmanager.netballmanager.dto.MatchResponse;
import com.netballmanager.netballmanager.dto.MatchResultRequest;
import com.netballmanager.netballmanager.domain.MatchStatus;
import com.netballmanager.netballmanager.service.GameMatchService;
import com.netballmanager.netballmanager.service.CurrentUserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MatchController {
    private final GameMatchService matches;
    private final CurrentUserService currentUser;

    public MatchController(GameMatchService matches, CurrentUserService currentUser) {
        this.matches = matches;
        this.currentUser = currentUser;
    }

    @PostMapping("/teams/{teamId}/matches")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COACH')")
    public MatchResponse create(@PathVariable Long teamId, @Valid @RequestBody MatchRequest request) {
        return matches.create(teamId, request);
    }

    @GetMapping("/teams/{teamId}/matches")
    public List<MatchResponse> list(@PathVariable Long teamId,
                                   @RequestParam(required = false) MatchStatus status,
                                   Principal principal) {
        currentUser.assertTeamAccess(principal.getName(), teamId);
        return matches.list(teamId, status);
    }

    @GetMapping("/matches")
    public List<MatchResponse> listByTeam(@RequestParam Long teamId,
                                          @RequestParam(required = false) MatchStatus status,
                                          java.security.Principal principal) {
        currentUser.assertTeamAccess(principal.getName(), teamId);
        return matches.list(teamId, status);
    }

    @GetMapping("/matches/upcoming")
    public List<MatchResponse> upcoming(@RequestParam Long teamId, java.security.Principal principal) {
        currentUser.assertTeamAccess(principal.getName(), teamId);
        return matches.list(teamId, MatchStatus.SCHEDULED);
    }

    @GetMapping("/matches/completed")
    public List<MatchResponse> completed(@RequestParam Long teamId, java.security.Principal principal) {
        currentUser.assertTeamAccess(principal.getName(), teamId);
        return matches.list(teamId, MatchStatus.COMPLETED);
    }

    @GetMapping("/matches/{id}")
    public MatchResponse get(@PathVariable Long id, Principal principal) {
        MatchResponse match = matches.get(id);
        currentUser.assertTeamAccess(principal.getName(), match.teamId());
        return match;
    }

    @PutMapping("/matches/{id}")
    @PreAuthorize("hasRole('COACH')")
    public MatchResponse update(@PathVariable Long id, @Valid @RequestBody MatchRequest request) {
        return matches.update(id, request);
    }

    @DeleteMapping("/matches/{id}")
    @PreAuthorize("hasRole('COACH')")
    public MatchResponse cancel(@PathVariable Long id) {
        return matches.cancel(id);
    }

    @PostMapping("/matches/{id}/result")
    @PreAuthorize("hasRole('COACH')")
    public MatchResponse result(@PathVariable Long id, @Valid @RequestBody MatchResultRequest request) {
        return matches.recordResult(id, request.teamScore(), request.opponentScore());
    }
}
