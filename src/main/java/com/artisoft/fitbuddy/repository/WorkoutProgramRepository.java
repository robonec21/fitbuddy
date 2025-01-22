package com.artisoft.fitbuddy.repository;

import com.artisoft.fitbuddy.model.WorkoutProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutProgramRepository extends JpaRepository<WorkoutProgram, Long> {
    List<WorkoutProgram> findByUserId(Long userId);

    @Query("SELECT wp, MAX(pl.date) as lastLogDate " +
            "FROM WorkoutProgram wp " +
            "JOIN ProgressLog pl ON pl.workoutProgram = wp " +
            "WHERE wp.user.id = :userId " +
            "GROUP BY wp " +
            "ORDER BY lastLogDate DESC")
    List<Object[]> findProgramsWithLastLogDate(@Param("userId") Long userId);

    @Query("SELECT wp FROM WorkoutProgram wp " +
            "WHERE wp.user.id = :userId " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM ProgressLog pl " +
            "    WHERE pl.workoutProgram = wp" +
            ")")
    List<WorkoutProgram> findProgramsWithoutLogs(@Param("userId") Long userId);
}
