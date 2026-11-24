package com.netballmanager.netballmanager.repository;

import com.netballmanager.netballmanager.domain.Substitution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubstitutionRepository extends JpaRepository<Substitution, Long> {
    List<Substitution> findAllByMatchIdOrderBySubstitutedAt(Long matchId);
}
