package com.artisoft.fitbuddy.service;

import com.artisoft.fitbuddy.dto.ExerciseDto;
import com.artisoft.fitbuddy.dto.ExerciseSearchCriteria;
import com.artisoft.fitbuddy.dto.ExerciseUsageDto;
import com.artisoft.fitbuddy.model.Exercise;
import com.artisoft.fitbuddy.model.WorkoutDay;
import com.artisoft.fitbuddy.model.WorkoutDayExercise;
import com.artisoft.fitbuddy.model.WorkoutProgram;
import com.artisoft.fitbuddy.repository.ExerciseRepository;
import com.artisoft.fitbuddy.repository.UserRepository;
import com.artisoft.fitbuddy.repository.WorkoutDayExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final WorkoutDayExerciseRepository workoutDayExerciseRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExerciseDto createExercise(ExerciseDto dto, String username) {
        // Verify user exists
        userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Exercise exercise = new Exercise();
        updateExerciseFromDto(exercise, dto);
        exercise = exerciseRepository.save(exercise);
        return convertToDto(exercise);
    }

    @Transactional
    public ExerciseDto updateExercise(Long id, ExerciseDto dto, String username) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

        // If exercise is used in any workout, verify user has access
        if (!exercise.getWorkoutDayExercises().isEmpty() &&
                exercise.getWorkoutDayExercises().stream()
                        .anyMatch(wde -> !wde.getWorkoutDay().getWorkoutProgram().getUser().getUsername().equals(username))) {
            throw new IllegalArgumentException("Access denied");
        }

        updateExerciseFromDto(exercise, dto);
        exercise = exerciseRepository.save(exercise);
        return convertToDto(exercise);
    }

    @Transactional
    public void deleteExercise(Long id, String username) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

        List<WorkoutDayExercise> usages = workoutDayExerciseRepository.findByExerciseId(id);
        workoutDayExerciseRepository.deleteAll(usages);
        exerciseRepository.delete(exercise);
    }

    @Transactional
    public void deleteExercises(List<Long> ids, String username) {
        ids.forEach(id -> deleteExercise(id, username));
    }

    @Transactional(readOnly = true)
    public ExerciseDto getExercise(Long id, String username) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

        return convertToDto(exercise);
    }

    @Transactional(readOnly = true)
    public ExerciseUsageDto getExerciseUsage(Long id, String username) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

        List<WorkoutDayExercise> usages = workoutDayExerciseRepository.findByExerciseId(id);

        Map<WorkoutProgram, List<WorkoutDay>> programDays = usages.stream()
                .collect(Collectors.groupingBy(
                        wde -> wde.getWorkoutDay().getWorkoutProgram(),
                        Collectors.mapping(
                                WorkoutDayExercise::getWorkoutDay,
                                Collectors.toList()
                        )
                ));

        ExerciseUsageDto usageDto = new ExerciseUsageDto();
        usageDto.setExerciseId(exercise.getId());
        usageDto.setExerciseName(exercise.getName());

        List<ExerciseUsageDto.WorkoutProgramUsageDto> programUsages = new ArrayList<>();

        programDays.forEach((program, days) -> {
            ExerciseUsageDto.WorkoutProgramUsageDto programUsage =
                    new ExerciseUsageDto.WorkoutProgramUsageDto();
            programUsage.setProgramId(program.getId());
            programUsage.setProgramName(program.getName());

            List<ExerciseUsageDto.WorkoutDayUsageDto> dayUsages = days.stream()
                    .map(day -> {
                        ExerciseUsageDto.WorkoutDayUsageDto dayUsage =
                                new ExerciseUsageDto.WorkoutDayUsageDto();
                        dayUsage.setDayId(day.getId());
                        dayUsage.setDayOfWeek(day.getDayOfWeek().name());
                        return dayUsage;
                    })
                    .collect(Collectors.toList());

            programUsage.setDays(dayUsages);
            programUsages.add(programUsage);
        });

        usageDto.setPrograms(programUsages);
        return usageDto;
    }

    @Transactional(readOnly = true)
    public List<ExerciseDto> searchExercises(ExerciseSearchCriteria criteria, String username) {
        List<Exercise> exercises;

        if (criteria.getName() != null && !criteria.getName().isEmpty()) {
            exercises = exerciseRepository.findByNameContainingIgnoreCase(criteria.getName());
        } else {
            exercises = exerciseRepository.findAll();
        }

        return exercises.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExerciseDto> getExercisesForWorkoutDay(Long workoutDayId, String username) {
        return exerciseRepository.findByWorkoutDayId(workoutDayId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void updateExerciseFromDto(Exercise exercise, ExerciseDto dto) {
        exercise.setName(dto.getName());
        exercise.setDescription(dto.getDescription());
        exercise.setDefaultSets(dto.getDefaultSets());
        exercise.setDefaultRepsPerSet(dto.getDefaultRepsPerSet());
        exercise.setDefaultRestPeriodBetweenSets(dto.getDefaultRestPeriodBetweenSets());
        exercise.setMediaLink(dto.getMediaLink());
    }

    private ExerciseDto convertToDto(Exercise exercise) {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(exercise.getId());
        dto.setName(exercise.getName());
        dto.setDescription(exercise.getDescription());
        dto.setDefaultSets(exercise.getDefaultSets());
        dto.setDefaultRepsPerSet(exercise.getDefaultRepsPerSet());
        dto.setDefaultRestPeriodBetweenSets(exercise.getDefaultRestPeriodBetweenSets());
        dto.setMediaLink(exercise.getMediaLink());
        return dto;
    }
}
