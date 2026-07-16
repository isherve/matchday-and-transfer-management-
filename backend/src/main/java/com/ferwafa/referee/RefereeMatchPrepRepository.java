package com.ferwafa.referee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefereeMatchPrepRepository extends JpaRepository<RefereeMatchPrep, Long> {
    Optional<RefereeMatchPrep> findByFixtureIdAndRefereeId(Long fixtureId, Long refereeId);
}
