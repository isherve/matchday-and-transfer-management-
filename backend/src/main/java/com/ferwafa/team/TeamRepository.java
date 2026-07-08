package com.ferwafa.team;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByUsername(String username);
    boolean existsByUsername(String username);
}
