package com.ferwafa.report;

import com.ferwafa.common.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchReportRepository extends JpaRepository<MatchReport, Long> {
    List<MatchReport> findByFixtureId(Long fixtureId);
    List<MatchReport> findByFixtureIdAndStatus(Long fixtureId, ReportStatus status);
    List<MatchReport> findByStatus(ReportStatus status);
    List<MatchReport> findByTeamMemberMemberIdAndStatus(Long memberId, ReportStatus status);
}
