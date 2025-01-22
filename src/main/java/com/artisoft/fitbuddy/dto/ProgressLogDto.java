package com.artisoft.fitbuddy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProgressLogDto {
    private Long id;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String notes;
    private Long workoutProgramId;
    private Long workoutDayId;
    private String workoutDayName;  // e.g., "MONDAY"

    @Size(min = 1, message = "At least one exercise progress must be recorded")
    private List<ExerciseProgressDto> exerciseProgresses = new ArrayList<>();
}
