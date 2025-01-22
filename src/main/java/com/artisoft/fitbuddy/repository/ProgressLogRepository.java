package com.artisoft.fitbuddy.repository;

import com.artisoft.fitbuddy.model.ProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProgressLogRepository extends JpaRepository<ProgressLog, Long> {
    List<ProgressLog> findByWorkoutProgramUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT pl FROM ProgressLog pl " +
            "WHERE pl.workoutProgram.id = :programId " +
            "AND pl.date >= :startDate AND pl.date <= :endDate " +
            "ORDER BY pl.date DESC")
    List<ProgressLog> findByProgramIdAndDateRange(Long programId, LocalDate startDate, LocalDate endDate);

    List<ProgressLog> findByWorkoutProgramIdOrderByDateDesc(Long programId);

    boolean existsByWorkoutProgramIdAndDate(Long programId, LocalDate date);
}
