package com.artisoft.fitbuddy.service;

import com.artisoft.fitbuddy.dto.WorkoutDayExerciseDto;
import com.artisoft.fitbuddy.dto.WorkoutProgramWithLogsDto;
import com.artisoft.fitbuddy.model.*;
import com.artisoft.fitbuddy.repository.*;
import com.artisoft.fitbuddy.dto.WorkoutDayDto;
import com.artisoft.fitbuddy.dto.WorkoutProgramDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutProgramService {
    private final WorkoutProgramRepository workoutProgramRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutDayExerciseRepository workoutDayExerciseRepository;

    @Transactional
    public WorkoutProgramDto createWorkoutProgram(WorkoutProgramDto dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WorkoutProgram program = new WorkoutProgram();
        program.setName(dto.getName());
        program.setDescription(dto.getDescription());
        program.setUser(user);

        // Save the program first to get its ID
        program = workoutProgramRepository.save(program);

        // Process workout days
        List<WorkoutDay> workoutDays = new ArrayList<>();
        if (dto.getWorkoutDays() != null) {
            for (WorkoutDayDto dayDto : dto.getWorkoutDays()) {
                WorkoutDay day = createWorkoutDay(dayDto, program);
                workoutDays.add(day);
            }
        }
        program.setWorkoutDays(workoutDays);

        return convertToDto(program);
    }

    @Transactional
    public WorkoutProgramDto updateWorkoutProgram(Long id, WorkoutProgramDto dto, String username) {
        WorkoutProgram program = workoutProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout program not found"));

        if (!program.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        program.setName(dto.getName());
        program.setDescription(dto.getDescription());

        // Update existing days and add new ones
        Map<Long, WorkoutDay> existingDays = program.getWorkoutDays().stream()
                .collect(Collectors.toMap(WorkoutDay::getId, day -> day));

        List<WorkoutDay> updatedDays = new ArrayList<>();

        for (WorkoutDayDto dayDto : dto.getWorkoutDays()) {
            if (dayDto.getId() != null && existingDays.containsKey(dayDto.getId())) {
                // Update existing day
                WorkoutDay existingDay = existingDays.get(dayDto.getId());
                updateWorkoutDay(existingDay, dayDto);
                updatedDays.add(existingDay);
                existingDays.remove(dayDto.getId());
            } else {
                // Create new day
                WorkoutDay newDay = createWorkoutDay(dayDto, program);
                updatedDays.add(newDay);
            }
        }

        // Remove days that are no longer present
        existingDays.values().forEach(day -> workoutDayRepository.delete(day));

        program.setWorkoutDays(updatedDays);
        program = workoutProgramRepository.save(program);

        return convertToDto(program);
    }

    @Transactional
    public WorkoutProgramDto updateProgramWorkoutDay(Long programId, Long dayId, WorkoutDayDto workoutDayDto, String username) {
        WorkoutProgram program = workoutProgramRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Workout program not found"));

        // Verify ownership
        if (!program.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        WorkoutDay existingDay = program.getWorkoutDays().stream()
                .filter(day -> day.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Workout day not found"));

        // Use existing updateWorkoutDay method
        updateWorkoutDay(existingDay, workoutDayDto);

        // Save the program to persist changes
        program = workoutProgramRepository.save(program);

        return convertToDto(program);
    }

    @Transactional(readOnly = true)
    public List<WorkoutProgramDto> getUserWorkoutPrograms(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return workoutProgramRepository.findByUserId(user.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkoutProgramDto getWorkoutProgram(Long id, String username) {
        WorkoutProgram program = workoutProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout program not found"));

        if (!program.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        return convertToDto(program);
    }

    @Transactional
    public void deleteWorkoutProgram(Long id, String username) {
        WorkoutProgram program = workoutProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout program not found"));

        if (!program.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        workoutProgramRepository.delete(program);
    }

    @Transactional(readOnly = true)
    public List<WorkoutProgramWithLogsDto> getWorkoutProgramsWithLogs(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Object[]> results = workoutProgramRepository.findProgramsWithLastLogDate(user.getId());

        return results.stream()
                .map(result -> {
                    WorkoutProgram program = (WorkoutProgram) result[0];
                    LocalDate lastLogDate = (LocalDate) result[1];

                    WorkoutProgramWithLogsDto dto = new WorkoutProgramWithLogsDto();
                    dto.setId(program.getId());
                    dto.setName(program.getName());
                    dto.setDescription(program.getDescription());
                    dto.setLastLogDate(lastLogDate);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutProgramDto> getWorkoutProgramsWithoutLogs(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<WorkoutProgram> programs = workoutProgramRepository.findProgramsWithoutLogs(user.getId());

        return programs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private WorkoutDay createWorkoutDay(WorkoutDayDto dayDto, WorkoutProgram program) {
        WorkoutDay day = new WorkoutDay();
        day.setDayOfWeek(WorkoutDay.DayOfWeek.valueOf(dayDto.getDayOfWeek()));
        day.setWorkoutProgram(program);
        day = workoutDayRepository.save(day);

        List<WorkoutDayExercise> exercises = new ArrayList<>();
        if (dayDto.getExercises() != null) {
            for (WorkoutDayExerciseDto exerciseDto : dayDto.getExercises()) {
                Exercise exercise = exerciseRepository.findById(exerciseDto.getExerciseId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Exercise not found: " + exerciseDto.getExerciseId()));

                WorkoutDayExercise workoutDayExercise = new WorkoutDayExercise();
                workoutDayExercise.setWorkoutDay(day);
                workoutDayExercise.setExercise(exercise);
                workoutDayExercise.setOrderIndex(exerciseDto.getOrderIndex());
                workoutDayExercise.setSets(exerciseDto.getSets() != null ?
                        exerciseDto.getSets() : exercise.getDefaultSets());
                workoutDayExercise.setRepsPerSet(exerciseDto.getRepsPerSet() != null ?
                        exerciseDto.getRepsPerSet() : exercise.getDefaultRepsPerSet());
                workoutDayExercise.setRestPeriodBetweenSets(exerciseDto.getRestPeriodBetweenSets() != null ?
                        exerciseDto.getRestPeriodBetweenSets() : exercise.getDefaultRestPeriodBetweenSets());
                workoutDayExercise.setNotes(exerciseDto.getNotes());

                exercises.add(workoutDayExercise);
            }
        }

        exercises = workoutDayExerciseRepository.saveAll(exercises);
        day.setWorkoutExercises(exercises);

        return day;
    }

    private void updateWorkoutDay(WorkoutDay existingDay, WorkoutDayDto dayDto) {
        existingDay.setDayOfWeek(WorkoutDay.DayOfWeek.valueOf(dayDto.getDayOfWeek()));

        // Create new exercises list
        List<WorkoutDayExercise> updatedExercises = new ArrayList<>();
        for (WorkoutDayExerciseDto exerciseDto : dayDto.getExercises()) {
            Exercise exercise = exerciseRepository.findById(exerciseDto.getExerciseId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Exercise not found: " + exerciseDto.getExerciseId()));

            WorkoutDayExercise workoutDayExercise = new WorkoutDayExercise();
            workoutDayExercise.setExercise(exercise);
            workoutDayExercise.setOrderIndex(exerciseDto.getOrderIndex());
            workoutDayExercise.setSets(exerciseDto.getSets() != null ?
                    exerciseDto.getSets() : exercise.getDefaultSets());
            workoutDayExercise.setRepsPerSet(exerciseDto.getRepsPerSet() != null ?
                    exerciseDto.getRepsPerSet() : exercise.getDefaultRepsPerSet());
            workoutDayExercise.setRestPeriodBetweenSets(exerciseDto.getRestPeriodBetweenSets() != null ?
                    exerciseDto.getRestPeriodBetweenSets() : exercise.getDefaultRestPeriodBetweenSets());
            workoutDayExercise.setNotes(exerciseDto.getNotes());

            updatedExercises.add(workoutDayExercise);
        }

        // Use the helper method to update the exercises
        existingDay.setWorkoutExercises(updatedExercises);

        // The save will happen through the transaction in the calling method
    }

    private WorkoutProgramDto convertToDto(WorkoutProgram program) {
        WorkoutProgramDto dto = new WorkoutProgramDto();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setDescription(program.getDescription());

        List<WorkoutDayDto> workoutDays = program.getWorkoutDays().stream()
                .sorted(Comparator.comparing(day -> day.getDayOfWeek().ordinal()))
                .map(this::convertToWorkoutDayDto)
                .collect(Collectors.toList());

        dto.setWorkoutDays(workoutDays);
        return dto;
    }

    private WorkoutDayDto convertToWorkoutDayDto(WorkoutDay day) {
        WorkoutDayDto dto = new WorkoutDayDto();
        dto.setId(day.getId());
        dto.setDayOfWeek(day.getDayOfWeek().name());

        List<WorkoutDayExerciseDto> exercises = day.getWorkoutExercises().stream()
                .sorted(Comparator.comparing(WorkoutDayExercise::getOrderIndex))
                .map(this::convertToWorkoutDayExerciseDto)
                .collect(Collectors.toList());

        dto.setExercises(exercises);
        return dto;
    }

    private WorkoutDayExerciseDto convertToWorkoutDayExerciseDto(WorkoutDayExercise workoutDayExercise) {
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
