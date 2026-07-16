package com.ferwafa.referee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefereeDiaryRepository extends JpaRepository<RefereeDiary, Long> {
    List<RefereeDiary> findByRefereeIdOrderByEntryDateDescCreatedAtDesc(Long refereeId);
}
