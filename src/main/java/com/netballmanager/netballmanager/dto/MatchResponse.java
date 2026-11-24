package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.MatchStatus;
import com.netballmanager.netballmanager.domain.VenueType;
import java.time.LocalDateTime;

public record MatchResponse(Long id, Long teamId, String opponentName, String venue,
                            LocalDateTime matchDateTime, VenueType venueType, MatchStatus status,
                            Integer teamScore, Integer opponentScore) {
}
