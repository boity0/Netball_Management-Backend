package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.PlayerRequest;
import com.netballmanager.netballmanager.dto.PlayerResponse;
import com.netballmanager.netballmanager.domain.Player;
import com.netballmanager.netballmanager.domain.PlayerStatus;
import com.netballmanager.netballmanager.domain.Team;
import com.netballmanager.netballmanager.exception.ConflictException;
import com.netballmanager.netballmanager.exception.NotFoundException;
import com.netballmanager.netballmanager.repository.NetballUserRepository;
import com.netballmanager.netballmanager.repository.PlayerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlayerService {
    private final PlayerRepository players;
    private final NetballUserRepository users;
    private final TeamService teams;

    public PlayerService(PlayerRepository players, NetballUserRepository users, TeamService teams) {
        this.players = players;
        this.users = users;
        this.teams = teams;
    }

    public PlayerResponse create(Long teamId, PlayerRequest request) {
        Team team = teams.requireForUpdate(teamId);
        if (players.findAllByTeamIdAndStatus(teamId, PlayerStatus.ACTIVE).size() >= 11) {
            throw new ConflictException("A team can have at most 11 active players");
        }
        if (players.existsByTeamIdAndJerseyNumber(teamId, request.jerseyNumber())) {
            throw new ConflictException("That jersey number is already assigned in this team");
        }
        if (players.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A player with that email already exists");
        }
        Player player = players.save(DomainFactory.createPlayer(request.firstName().trim(), request.lastName().trim(),
                request.email().trim().toLowerCase(), request.phoneNumber(), request.jerseyNumber(),
                request.preferredPositions(), team));
        users.findByEmailIgnoreCase(player.getEmail()).ifPresent(user -> {
            user.setPlayer(player);
            users.save(user);
        });
        return toResponse(player);
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> list(Long teamId) {
        teams.require(teamId);
        return players.findAllByTeamIdOrderByJerseyNumber(teamId).stream().map(PlayerService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlayerResponse get(Long id) {
        return toResponse(require(id));
    }

    public PlayerResponse update(Long id, PlayerRequest request) {
        Player player = require(id);
        if (players.existsByTeamIdAndJerseyNumberAndIdNot(
                player.getTeam().getId(), request.jerseyNumber(), id)) {
            throw new ConflictException("That jersey number is already assigned in this team");
        }
        if (!player.getEmail().equalsIgnoreCase(request.email())
                && players.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A player with that email already exists");
        }
        if (!player.getEmail().equalsIgnoreCase(request.email())
                && users.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("An account with that email already exists");
        }
        boolean reactivating = player.getStatus() == PlayerStatus.INACTIVE
                && request.status() == PlayerStatus.ACTIVE;
        if (reactivating) {
            teams.requireForUpdate(player.getTeam().getId());
            if (players.findAllByTeamIdAndStatus(player.getTeam().getId(), PlayerStatus.ACTIVE).size() >= 11) {
                throw new ConflictException("A team can have at most 11 active players");
            }
        }
        String oldEmail = player.getEmail();
        player.setFirstName(request.firstName().trim());
        player.setLastName(request.lastName().trim());
        player.setEmail(request.email().trim().toLowerCase());
        player.setPhoneNumber(request.phoneNumber());
        player.setJerseyNumber(request.jerseyNumber());
        player.setPreferredPositions(request.preferredPositions());
        if (request.status() != null) {
            player.setStatus(request.status());
        }
        if (!oldEmail.equalsIgnoreCase(player.getEmail())) {
            users.findByEmailIgnoreCase(oldEmail).ifPresent(user -> user.setEmail(player.getEmail()));
        }
        return toResponse(player);
    }

    public void deactivate(Long id) {
        require(id).setStatus(PlayerStatus.INACTIVE);
    }

    public Player require(Long id) {
        return players.findById(id).orElseThrow(() -> new NotFoundException("Player " + id + " was not found"));
    }

    private static PlayerResponse toResponse(Player player) {
        return new PlayerResponse(player.getId(), player.getTeam().getId(), player.getFirstName(),
                player.getLastName(), player.getEmail(), player.getPhoneNumber(), player.getJerseyNumber(),
                player.getPreferredPositions(), player.getStatus(), player.getRegistrationDate());
    }
}
