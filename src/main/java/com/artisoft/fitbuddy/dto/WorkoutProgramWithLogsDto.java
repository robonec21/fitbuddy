package com.artisoft.fitbuddy.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutProgramWithLogsDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate lastLogDate;
}
