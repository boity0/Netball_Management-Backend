package com.netballmanager.netballmanager.repository;

import com.netballmanager.netballmanager.domain.Attendance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findAllByPlayerIdOrderByIdDesc(Long playerId);
    List<Attendance> findAllByPlayerId(Long playerId);
    Optional<Attendance> findByMatchIdAndPlayerId(Long matchId, Long playerId);
    Optional<Attendance> findByTrainingIdAndPlayerId(Long trainingId, Long playerId);
}
