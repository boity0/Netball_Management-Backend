package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.TeamRequest;
import com.netballmanager.netballmanager.dto.TeamResponse;
import com.netballmanager.netballmanager.domain.Team;
import com.netballmanager.netballmanager.exception.ConflictException;
import com.netballmanager.netballmanager.exception.NotFoundException;
import com.netballmanager.netballmanager.repository.TeamRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamService {
    private final TeamRepository teams;

    public TeamService(TeamRepository teams) {
        this.teams = teams;
    }

    public TeamResponse create(TeamRequest request) {
        if (teams.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("A team with that name already exists");
        }
        return toResponse(teams.save(DomainFactory.createTeam(request.name().trim(), request.description())));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> list() {
        return teams.findAll().stream().map(TeamService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse get(Long id) {
        return toResponse(require(id));
    }

    public TeamResponse update(Long id, TeamRequest request) {
        Team team = require(id);
        if (!team.getName().equalsIgnoreCase(request.name().trim())
                && teams.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("A team with that name already exists");
        }
        team.setName(request.name().trim());
        team.setDescription(request.description());
        return toResponse(team);
    }

    public Team require(Long id) {
        return teams.findById(id).orElseThrow(() -> new NotFoundException("Team " + id + " was not found"));
    }

    public Team requireForUpdate(Long id) {
        return teams.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Team " + id + " was not found"));
    }

    private static TeamResponse toResponse(Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.getDescription(), team.getCreatedAt());
    }
}
