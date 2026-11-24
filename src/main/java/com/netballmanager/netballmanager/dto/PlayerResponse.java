package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.PlayerStatus;
import com.netballmanager.netballmanager.domain.Position;
import java.time.LocalDate;
import java.util.Set;

public record PlayerResponse(Long id, Long teamId, String firstName, String lastName, String email,
                             String phoneNumber, Integer jerseyNumber, Set<Position> preferredPositions,
                             PlayerStatus status, LocalDate registrationDate) {
}
