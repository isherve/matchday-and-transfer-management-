package com.ferwafa.sanction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommissionerReportRepository extends JpaRepository<CommissionerReport, Long> {
    Optional<CommissionerReport> findByFixtureId(Long fixtureId);
    List<CommissionerReport> findByStatus(String status);
}
