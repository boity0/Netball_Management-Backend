package com.netballmanager.netballmanager.repository;

import com.netballmanager.netballmanager.domain.PlayerAvailability;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerAvailabilityRepository extends JpaRepository<PlayerAvailability, Long> {
    Optional<PlayerAvailability> findByMatchIdAndPlayerId(Long matchId, Long playerId);
    List<PlayerAvailability> findAllByMatchId(Long matchId);
}
