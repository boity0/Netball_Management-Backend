package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.domain.NetballUser;
import com.netballmanager.netballmanager.domain.UserRole;
import com.netballmanager.netballmanager.exception.BadRequestException;
import com.netballmanager.netballmanager.exception.NotFoundException;
import com.netballmanager.netballmanager.repository.NetballUserRepository;
import com.netballmanager.netballmanager.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CurrentUserService {
    private final NetballUserRepository users;
    private final TeamRepository teams;

    public CurrentUserService(NetballUserRepository users, TeamRepository teams) {
        this.users = users;
        this.teams = teams;
    }

    public NetballUser require(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow(() ->
                new NotFoundException("Authenticated account was not found"));
    }

    public Long requireLinkedPlayerId(String email) {
        NetballUser user = require(email);
        if (user.getPlayer() == null) {
            throw new NotFoundException("No player profile is linked to this account yet");
        }
        return user.getPlayer().getId();
    }

    public Long requireLinkedTeamId(String email) {
        NetballUser user = require(email);
        if (user.getPlayer() == null) {
            throw new BadRequestException("This account is not linked to a player profile yet");
        }
        return user.getPlayer().getTeam().getId();
    }

    public Long requireOwnPlayerId(String email, Long requestedPlayerId) {
        NetballUser user = require(email);
        if (user.getRole() == UserRole.COACH) {
            return requestedPlayerId;
        }
        if (user.getPlayer() == null) {
            throw new BadRequestException("This account is not linked to a player profile yet");
        }
        if (!user.getPlayer().getId().equals(requestedPlayerId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Players may only access their own information");
        }
        return requestedPlayerId;
    }

    public void assertTeamAccess(String email, Long teamId) {
        NetballUser user = require(email);
        if (user.getRole() == UserRole.COACH) {
            return;
        }
        if (user.getPlayer() == null) {
            throw new BadRequestException("This account is not linked to a player profile yet");
        }
        if (!teams.existsById(teamId)) {
            throw new NotFoundException("Team " + teamId + " was not found");
        }
        if (!user.getPlayer().getTeam().getId().equals(teamId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Players may only access their own team");
        }
    }
}
