package com.artisoft.fitbuddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkoutDayExerciseDto {
    private Long id;
    private Long exerciseId;
    private String exerciseName;  // For convenience

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;

    @NotNull(message = "Sets is required")
    @Min(value = 1, message = "Sets must be at least 1")
    private Integer sets;

    @NotNull(message = "Reps per set is required")
    @Min(value = 1, message = "Reps must be at least 1")
    private Integer repsPerSet;

    @Min(value = 0, message = "Rest period cannot be negative")
    private Integer restPeriodBetweenSets;

    private String notes;
}
