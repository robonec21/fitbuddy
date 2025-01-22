package com.artisoft.fitbuddy.service;

import com.artisoft.fitbuddy.dto.WorkoutDayDto;
import com.artisoft.fitbuddy.dto.WorkoutDayExerciseDto;
import com.artisoft.fitbuddy.model.Exercise;
import com.artisoft.fitbuddy.model.WorkoutDay;
import com.artisoft.fitbuddy.model.WorkoutDayExercise;
import com.artisoft.fitbuddy.repository.ExerciseRepository;
import com.artisoft.fitbuddy.repository.WorkoutDayExerciseRepository;
import com.artisoft.fitbuddy.repository.WorkoutDayRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutDayService {
    private final WorkoutDayRepository workoutDayRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutDayExerciseRepository workoutDayExerciseRepository;

    @Transactional
    public WorkoutDayDto addExercisesToWorkoutDay(Long workoutDayId, List<WorkoutDayExerciseDto> exerciseDtos, String username) {
        WorkoutDay workoutDay = workoutDayRepository.findById(workoutDayId)
                .orElseThrow(() -> new IllegalArgumentException("Workout day not found"));

        // Verify ownership
        if (!workoutDay.getWorkoutProgram().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        List<WorkoutDayExercise> newExercises = new ArrayList<>();

        for (WorkoutDayExerciseDto exerciseDto : exerciseDtos) {
            Exercise exercise = exerciseRepository.findById(exerciseDto.getExerciseId())
                    .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + exerciseDto.getExerciseId()));

            WorkoutDayExercise workoutDayExercise = new WorkoutDayExercise();
            workoutDayExercise.setWorkoutDay(workoutDay);
            workoutDayExercise.setExercise(exercise);
            workoutDayExercise.setOrderIndex(exerciseDto.getOrderIndex());
            workoutDayExercise.setSets(exerciseDto.getSets());
            workoutDayExercise.setRepsPerSet(exerciseDto.getRepsPerSet());
            workoutDayExercise.setRestPeriodBetweenSets(exerciseDto.getRestPeriodBetweenSets());
            workoutDayExercise.setNotes(exerciseDto.getNotes());

            newExercises.add(workoutDayExercise);
        }

        workoutDayExerciseRepository.saveAll(newExercises);

        return convertToDto(workoutDay);
    }

    @Transactional
    public WorkoutDayDto updateWorkoutDayExercise(Long workoutDayId, Long exerciseId, WorkoutDayExerciseDto dto, String username) {
        WorkoutDay workoutDay = workoutDayRepository.findById(workoutDayId)
                .orElseThrow(() -> new IllegalArgumentException("Workout day not found"));

        // Verify ownership
        if (!workoutDay.getWorkoutProgram().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        WorkoutDayExercise workoutDayExercise = workoutDay.getWorkoutExercises().stream()
                .filter(wde -> wde.getExercise().getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found in workout day"));

        workoutDayExercise.setSets(dto.getSets());
        workoutDayExercise.setRepsPerSet(dto.getRepsPerSet());
        workoutDayExercise.setRestPeriodBetweenSets(dto.getRestPeriodBetweenSets());
        workoutDayExercise.setOrderIndex(dto.getOrderIndex());
        workoutDayExercise.setNotes(dto.getNotes());

        workoutDayExerciseRepository.save(workoutDayExercise);

        return convertToDto(workoutDay);
    }

    private WorkoutDayDto convertToDto(WorkoutDay workoutDay) {
        WorkoutDayDto dto = new WorkoutDayDto();
        dto.setId(workoutDay.getId());
        dto.setDayOfWeek(workoutDay.getDayOfWeek().name());

        List<WorkoutDayExerciseDto> exerciseDtos = workoutDay.getWorkoutExercises().stream()
                .map(this::convertToExerciseDto)
                .collect(Collectors.toList());

        dto.setExercises(exerciseDtos);
        return dto;
    }

    private WorkoutDayExerciseDto convertToExerciseDto(WorkoutDayExercise workoutDayExercise) {
        WorkoutDayExerciseDto dto = new WorkoutDayExerciseDto();
        dto.setId(workoutDayExercise.getId());
        dto.setExerciseId(workoutDayExercise.getExercise().getId());
        dto.setExerciseName(workoutDayExercise.getExercise().getName());
        dto.setOrderIndex(workoutDayExercise.getOrderIndex());
        dto.setSets(workoutDayExercise.getSets());
        dto.setRepsPerSet(workoutDayExercise.getRepsPerSet());
        dto.setRestPeriodBetweenSets(workoutDayExercise.getRestPeriodBetweenSets());
        dto.setNotes(workoutDayExercise.getNotes());
        return dto;
    }
}
