package com.netballmanager.netballmanager.repository;

import com.netballmanager.netballmanager.domain.Player;
import com.netballmanager.netballmanager.domain.PlayerStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findAllByTeamIdOrderByJerseyNumber(Long teamId);
    List<Player> findAllByTeamIdAndStatus(Long teamId, PlayerStatus status);
    Optional<Player> findByIdAndTeamId(Long id, Long teamId);
    boolean existsByTeamIdAndJerseyNumber(Long teamId, Integer jerseyNumber);
    boolean existsByTeamIdAndJerseyNumberAndIdNot(Long teamId, Integer jerseyNumber, Long id);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Player> findByEmailIgnoreCase(String email);
}
