package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.Position;
import java.time.LocalDateTime;

public record SubstitutionResponse(Long id, Long matchId, Long playerOutId, String playerOutName,
                                   Long playerInId, String playerInName, Position previousPosition,
                                   Position newPosition,
                                   int period, LocalDateTime substitutedAt) {
}
