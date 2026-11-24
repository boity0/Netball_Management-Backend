package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(@NotNull Long playerId, @NotNull AttendanceStatus status) {
}
