package com.netballmanager.netballmanager.dto;

import com.netballmanager.netballmanager.domain.AvailabilityStatus;
import java.util.List;
import java.util.Map;

public record AvailabilitySummary(int availableCount, List<PlayerResponse> unavailable,
                                  List<PlayerResponse> noResponse, Map<Long, AvailabilityStatus> responses,
                                  String message) {
}
