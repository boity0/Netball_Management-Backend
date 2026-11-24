package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.Position;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubstitutionRequest(@NotNull Long playerOutId, @NotNull Long playerInId,
                                 @NotNull Position position, @Min(1) @Max(4) int period) {
}
