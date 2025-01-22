package com.artisoft.fitbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class ExerciseProgressDto {
    private Long id;
    private Long workoutDayExerciseId;  // ID of the planned exercise in the workout
    private Long replacementExerciseId;  // ID of the replacement exercise (if any)
    private String exerciseName;         // Name of the actual exercise performed

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    @NotNull(message = "Actual sets is required")
    private Integer actualSets;

    @Size(min = 1, message = "At least one set must be recorded")
    private List<Integer> repsPerSet = new ArrayList<>();

    private List<Double> weightPerSet = new ArrayList<>();
    private Integer restPeriodBetweenSets;
    private Boolean completed = false;
    private Boolean skipped = false;
    private String notes;
}
