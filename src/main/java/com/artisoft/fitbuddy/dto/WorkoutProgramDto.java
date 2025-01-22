package com.artisoft.fitbuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class WorkoutProgramDto {
    private Long id;

    @NotBlank(message = "Program name is required")
    private String name;

    private String description;

    @Size(min = 1, message = "At least one workout day is required")
    private List<WorkoutDayDto> workoutDays;
}
