package com.artisoft.fitbuddy.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExerciseUsageDto {
    private Long exerciseId;
    private String exerciseName;
    private List<WorkoutProgramUsageDto> programs;

    @Data
    public static class WorkoutProgramUsageDto {
        private Long programId;
        private String programName;
        private List<WorkoutDayUsageDto> days;
    }

    @Data
    public static class WorkoutDayUsageDto {
        private Long dayId;
        private String dayOfWeek;
    }
}
