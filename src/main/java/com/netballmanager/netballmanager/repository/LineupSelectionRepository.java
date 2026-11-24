package com.netballmanager.netballmanager.repository;

import com.netballmanager.netballmanager.domain.LineupSelection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineupSelectionRepository extends JpaRepository<LineupSelection, Long> {
    long countByPlayerIdAndStartingTrue(Long playerId);
}
