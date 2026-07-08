package com.ferwafa.transfer;

import com.ferwafa.common.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByStatus(TransferStatus status);

    @Query("SELECT t FROM Transfer t WHERE t.teamFrom.teamId = :teamId OR t.teamTo.teamId = :teamId ORDER BY t.requestDate DESC")
    List<Transfer> findByTeamInvolved(@Param("teamId") Long teamId);
}
