package com.artisoft.fitbuddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExerciseDto {
    private Long id;

    @NotBlank(message = "Exercise name is required")
    private String name;

    private String description;

    @NotNull(message = "Default sets is required")
    @Min(value = 1, message = "Default sets must be at least 1")
    private Integer defaultSets;

    @NotNull(message = "Default reps per set is required")
    @Min(value = 1, message = "Default reps must be at least 1")
    private Integer defaultRepsPerSet;

    @Min(value = 0, message = "Rest period cannot be negative")
    private Integer defaultRestPeriodBetweenSets;

    private String mediaLink;
}
