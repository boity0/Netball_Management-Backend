package com.netballmanager.netballmanager.repository;

import com.netballmanager.netballmanager.domain.MatchStatistics;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchStatisticsRepository extends JpaRepository<MatchStatistics, Long> {
    List<MatchStatistics> findAllByPlayerIdOrderByMatchMatchDateTimeDesc(Long playerId);
    Optional<MatchStatistics> findByMatchIdAndPlayerId(Long matchId, Long playerId);
}
