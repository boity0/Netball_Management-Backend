package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public record AvailabilityRequest(
        @NotNull Long playerId,
        @NotNull AvailabilityStatus status) {
}
