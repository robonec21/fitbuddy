package com.artisoft.fitbuddy.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgressLogSummaryDto {
    private Long id;
    private LocalDate date;
    private String workoutDayName;
    private Integer totalExercises;
    private Integer completedExercises;
    private Integer skippedExercises;
}
