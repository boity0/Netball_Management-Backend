package com.netballmanager.netballmanager.controller;

import com.netballmanager.netballmanager.dto.TeamRequest;
import com.netballmanager.netballmanager.dto.TeamResponse;
import com.netballmanager.netballmanager.domain.UserRole;
import com.netballmanager.netballmanager.service.CurrentUserService;
import com.netballmanager.netballmanager.service.TeamService;
import jakarta.validation.Valid;
import java.util.List;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teams;
    private final CurrentUserService currentUser;

    public TeamController(TeamService teams, CurrentUserService currentUser) {
        this.teams = teams;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COACH')")
    public TeamResponse create(@Valid @RequestBody TeamRequest request) {
        return teams.create(request);
    }

    @GetMapping
    public List<TeamResponse> list(Principal principal) {
        var user = currentUser.require(principal.getName());
        if (user.getRole() == UserRole.COACH) {
            return teams.list();
        }
        return List.of(teams.get(currentUser.requireLinkedTeamId(principal.getName())));
    }

    @GetMapping("/{id}")
    public TeamResponse get(@PathVariable Long id, Principal principal) {
        currentUser.assertTeamAccess(principal.getName(), id);
        return teams.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COACH')")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return teams.update(id, request);
    }
}
