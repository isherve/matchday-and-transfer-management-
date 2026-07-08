package com.ferwafa.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransferWindowRepository extends JpaRepository<TransferWindow, Long> {

    List<TransferWindow> findBySeasonOrderByOpenDateAsc(String season);

    @Query("""
        SELECT w FROM TransferWindow w
        WHERE w.active = true
          AND w.openDate <= :today
          AND w.closeDate >= :today
        ORDER BY w.openDate ASC
        """)
    List<TransferWindow> findOpenWindows(@Param("today") LocalDate today);

    @Query("""
        SELECT w FROM TransferWindow w
        WHERE w.active = true
          AND w.season = :season
          AND w.openDate <= :today
          AND w.closeDate >= :today
        """)
    Optional<TransferWindow> findOpenWindowForSeason(@Param("season") String season, @Param("today") LocalDate today);
}
