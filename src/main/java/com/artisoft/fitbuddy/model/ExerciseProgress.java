package com.artisoft.fitbuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exercise_progresses")
public class ExerciseProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "progress_log_id", nullable = false)
    private ProgressLog progressLog;

    @ManyToOne
    @JoinColumn(name = "workout_day_exercise_id")
    private WorkoutDayExercise plannedExercise;

    @ManyToOne
    @JoinColumn(name = "replacement_exercise_id")
    private Exercise replacementExercise;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private Integer actualSets;

    @ElementCollection
    @CollectionTable(
            name = "exercise_progress_set_details",
            joinColumns = @JoinColumn(name = "exercise_progress_id")
    )
    @OrderColumn(name = "set_number")
    private List<SetDetail> setDetails = new ArrayList<>();

    // Inner class to represent a single set's details
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetDetail {
        @Column(name = "reps_per_set")
        private Integer reps;

        @Column(name = "weight_per_set")
        private Double weight;
    }

    // Add methods to manage setDetails
    public void addSetDetail(Integer reps, Double weight) {
        setDetails.add(new SetDetail(reps, weight));
    }

    @Column(name = "rest_period_seconds")
    private Integer restPeriodBetweenSets;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private Boolean skipped = false;

    @Column(length = 500)
    private String notes;
}
