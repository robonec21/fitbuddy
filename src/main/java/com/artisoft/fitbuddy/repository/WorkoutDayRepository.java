package com.artisoft.fitbuddy.repository;

import com.artisoft.fitbuddy.model.WorkoutDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, Long> {
    List<WorkoutDay> findByWorkoutProgramId(Long programId);
}
