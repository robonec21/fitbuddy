package com.artisoft.fitbuddy.repository;

import com.artisoft.fitbuddy.model.WorkoutDayExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutDayExerciseRepository extends JpaRepository<WorkoutDayExercise, Long> {
    List<WorkoutDayExercise> findByWorkoutDayIdOrderByOrderIndexAsc(Long workoutDayId);

    @Query("SELECT wde FROM WorkoutDayExercise wde WHERE wde.workoutDay.workoutProgram.user.id = :userId")
    List<WorkoutDayExercise> findByUserId(Long userId);

    void deleteByWorkoutDayId(Long workoutDayId);

    @Query("SELECT wde FROM WorkoutDayExercise wde " +
            "LEFT JOIN FETCH wde.workoutDay wd " +
            "LEFT JOIN FETCH wd.workoutProgram wp " +
            "WHERE wde.exercise.id = :exerciseId")
    List<WorkoutDayExercise> findByExerciseId(@Param("exerciseId") Long exerciseId);
}
