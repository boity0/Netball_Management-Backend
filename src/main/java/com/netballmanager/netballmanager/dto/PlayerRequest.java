package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.PlayerStatus;
import com.netballmanager.netballmanager.domain.Position;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record PlayerRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email String email,
        @Size(max = 30) String phoneNumber,
        @NotNull @Min(1) @Max(99) Integer jerseyNumber,
        @NotEmpty Set<Position> preferredPositions,
        PlayerStatus status) {
}
