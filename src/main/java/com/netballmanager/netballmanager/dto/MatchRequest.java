package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.VenueType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MatchRequest(
        @NotBlank String opponentName,
        @NotBlank String venue,
        @NotNull @Future LocalDateTime matchDateTime,
        @NotNull VenueType venueType) {
}
