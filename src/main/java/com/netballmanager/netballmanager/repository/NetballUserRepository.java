package com.netballmanager.netballmanager.repository;

import com.netballmanager.netballmanager.domain.NetballUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetballUserRepository extends JpaRepository<NetballUser, Long> {
    Optional<NetballUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
