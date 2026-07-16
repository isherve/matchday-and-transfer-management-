package com.ferwafa.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchReportEditLogRepository extends JpaRepository<MatchReportEditLog, Long> {
    List<MatchReportEditLog> findByFixtureIdOrderByCreatedAtDesc(Long fixtureId);
}
