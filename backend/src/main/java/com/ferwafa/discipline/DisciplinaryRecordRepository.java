package com.ferwafa.discipline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DisciplinaryRecordRepository extends JpaRepository<DisciplinaryRecord, Long> {
    List<DisciplinaryRecord> findByTeamMemberMemberIdOrderByWeekAsc(Long memberId);

    @Query("SELECT d FROM DisciplinaryRecord d WHERE d.teamMember.memberId = :memberId AND d.suspensionReason IS NOT NULL AND d.suspensionServed = false")
    List<DisciplinaryRecord> findActiveSuspensions(@Param("memberId") Long memberId);

    @Query("SELECT d FROM DisciplinaryRecord d WHERE d.teamMember.team.teamId = :teamId AND d.suspensionReason IS NOT NULL")
    List<DisciplinaryRecord> findByTeamId(@Param("teamId") Long teamId);

    List<DisciplinaryRecord> findByTeamMemberMemberIdAndWeekLessThanEqualAndCardType(
            Long memberId, Integer week, com.ferwafa.common.CardType cardType);
}
