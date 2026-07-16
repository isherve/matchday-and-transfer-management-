package com.ferwafa.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchReportCommentRepository extends JpaRepository<MatchReportComment, Long> {
    List<MatchReportComment> findByFixtureIdOrderByCreatedAtAsc(Long fixtureId);
}
