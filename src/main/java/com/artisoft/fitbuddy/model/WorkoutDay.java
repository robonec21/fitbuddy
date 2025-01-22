package com.artisoft.fitbuddy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workout_days")
public class WorkoutDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @ManyToOne
    @JoinColumn(name = "workout_program_id")
    private WorkoutProgram workoutProgram;

    @OneToMany(mappedBy = "workoutDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<WorkoutDayExercise> workoutExercises = new ArrayList<>();

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    public void addExercise(WorkoutDayExercise exercise) {
        workoutExercises.add(exercise);
        exercise.setWorkoutDay(this);
    }

    public void removeExercise(WorkoutDayExercise exercise) {
        workoutExercises.remove(exercise);
        exercise.setWorkoutDay(null);
    }

    public void setWorkoutExercises(List<WorkoutDayExercise> exercises) {
        // Clear existing exercises
        this.workoutExercises.clear();

        if (exercises != null) {
            exercises.forEach(exercise -> {
                exercise.setWorkoutDay(this);
                this.workoutExercises.add(exercise);
            });
        }
    }
}
