package com.artisoft.fitbuddy.repository;

import com.artisoft.fitbuddy.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT e FROM Exercise e " +
            "JOIN e.workoutDayExercises wde " +
            "WHERE wde.workoutDay.id = :workoutDayId")
    List<Exercise> findByWorkoutDayId(Long workoutDayId);

    @Query("SELECT DISTINCT e FROM Exercise e " +
            "JOIN e.workoutDayExercises wde " +
            "JOIN wde.workoutDay wd " +
            "JOIN wd.workoutProgram wp " +
            "WHERE wp.user.id = :userId")
    List<Exercise> findByUserId(Long userId);

    @Query("SELECT CASE WHEN COUNT(wde) > 0 THEN true ELSE false END " +
            "FROM WorkoutDayExercise wde " +
            "WHERE wde.exercise.id = :exerciseId")
    boolean isExerciseUsedInWorkout(Long exerciseId);
}
