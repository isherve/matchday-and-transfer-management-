package com.ferwafa.referee;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RefereeRepository extends JpaRepository<Referee, Long> {
    Optional<Referee> findByEmail(String email);
    List<Referee> findByRefereeIdIn(List<Long> ids);
}
