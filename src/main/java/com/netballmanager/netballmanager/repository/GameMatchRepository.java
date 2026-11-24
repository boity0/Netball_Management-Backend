package com.netballmanager.netballmanager.repository;

import com.netballmanager.netballmanager.domain.GameMatch;
import com.netballmanager.netballmanager.domain.MatchStatus;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameMatchRepository extends JpaRepository<GameMatch, Long> {
    List<GameMatch> findAllByTeamIdOrderByMatchDateTime(Long teamId);
    List<GameMatch> findAllByTeamIdAndStatusOrderByMatchDateTime(Long teamId, MatchStatus status);
    List<GameMatch> findAllByTeamIdAndStatusAndMatchDateTimeAfterOrderByMatchDateTime(
            Long teamId, MatchStatus status, LocalDateTime after);
}
