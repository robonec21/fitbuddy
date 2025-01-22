package com.artisoft.fitbuddy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workout_day_exercises")
public class WorkoutDayExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDay workoutDay;

    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer orderIndex;  // Position in the workout day

    @Column(nullable = false)
    private Integer sets;

    @Column(nullable = false)
    private Integer repsPerSet;

    @Column(name = "rest_period_seconds")
    private Integer restPeriodBetweenSets;

    // Optional specific notes for this exercise in this workout
    @Column(length = 500)
    private String notes;
}
