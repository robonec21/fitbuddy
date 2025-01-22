package com.artisoft.fitbuddy.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WorkoutDayDto {
    private Long id;

    private String dayOfWeek;

    private List<WorkoutDayExerciseDto> exercises = new ArrayList<>();
}
