package com.artisoft.fitbuddy.service;

import com.artisoft.fitbuddy.dto.ExerciseProgressDto;
import com.artisoft.fitbuddy.dto.ProgressLogDto;
import com.artisoft.fitbuddy.dto.ProgressLogSummaryDto;
import com.artisoft.fitbuddy.model.*;
import com.artisoft.fitbuddy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressLogService {
    private final ProgressLogRepository progressLogRepository;
    private final WorkoutProgramRepository workoutProgramRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutDayExerciseRepository workoutDayExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProgressLogDto createProgressLog(ProgressLogDto dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WorkoutProgram workoutProgram = workoutProgramRepository.findById(dto.getWorkoutProgramId())
                .orElseThrow(() -> new IllegalArgumentException("Workout program not found"));

        if (!workoutProgram.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        WorkoutDay workoutDay = workoutDayRepository.findById(dto.getWorkoutDayId())
                .orElseThrow(() -> new IllegalArgumentException("Workout day not found"));

        ProgressLog progressLog = new ProgressLog();
        progressLog.setDate(dto.getDate());
        progressLog.setNotes(dto.getNotes());
        progressLog.setWorkoutProgram(workoutProgram);
        progressLog.setWorkoutDay(workoutDay);

        List<ExerciseProgress> exerciseProgresses = new ArrayList<>();
        for (ExerciseProgressDto progressDto : dto.getExerciseProgresses()) {
            ExerciseProgress progress = createExerciseProgress(progressDto, progressLog);
            exerciseProgresses.add(progress);
        }

        progressLog.setExerciseProgresses(exerciseProgresses);
        progressLog = progressLogRepository.save(progressLog);

        return convertToDto(progressLog);
    }

    @Transactional
    public ProgressLogDto updateProgressLog(Long id, ProgressLogDto dto, String username) {
        ProgressLog progressLog = progressLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Progress log not found"));

        if (!progressLog.getWorkoutProgram().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        progressLog.setNotes(dto.getNotes());

        // Remove existing progress entries
        progressLog.getExerciseProgresses().clear();

        // Add updated progress entries
        List<ExerciseProgress> exerciseProgresses = new ArrayList<>();
        for (ExerciseProgressDto progressDto : dto.getExerciseProgresses()) {
            ExerciseProgress progress = createExerciseProgress(progressDto, progressLog);
            exerciseProgresses.add(progress);
        }

        progressLog.setExerciseProgresses(exerciseProgresses);
        progressLog = progressLogRepository.save(progressLog);

        return convertToDto(progressLog);
    }

    @Transactional(readOnly = true)
    public List<ProgressLogSummaryDto> getUserProgressLogs(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ProgressLog> logs = progressLogRepository.findByWorkoutProgramUserIdAndDateBetween(
                user.getId(), startDate, endDate);

        return logs.stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProgressLogDto getProgressLog(Long id, String username) {
        ProgressLog progressLog = progressLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Progress log not found"));

        if (!progressLog.getWorkoutProgram().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        return convertToDto(progressLog);
    }

    @Transactional
    public void deleteProgressLog(Long id, String username) {
        ProgressLog progressLog = progressLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Progress log not found"));

        if (!progressLog.getWorkoutProgram().getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        progressLogRepository.delete(progressLog);
    }

    @Transactional(readOnly = true)
    public List<ProgressLogSummaryDto> getProgressLogSummaries(Long programId, String username) {
        WorkoutProgram program = workoutProgramRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found"));

        if (!program.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        List<ProgressLog> logs = progressLogRepository.findByWorkoutProgramIdOrderByDateDesc(programId);

        return logs.stream()
                .map(log -> {
                    ProgressLogSummaryDto summary = new ProgressLogSummaryDto();
                    summary.setId(log.getId());
                    summary.setDate(log.getDate());
                    summary.setWorkoutDayName(log.getWorkoutDay().getDayOfWeek().name());

                    int totalExercises = log.getExerciseProgresses().size();
                    int completedExercises = (int) log.getExerciseProgresses().stream()
                            .filter(ExerciseProgress::getCompleted)
                            .count();
                    int skippedExercises = (int) log.getExerciseProgresses().stream()
                            .filter(ExerciseProgress::getSkipped)
                            .count();

                    summary.setTotalExercises(totalExercises);
                    summary.setCompletedExercises(completedExercises);
                    summary.setSkippedExercises(skippedExercises);

                    return summary;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgressLogDto> getProgressLogs(Long programId, String username) {
        WorkoutProgram program = workoutProgramRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found"));

        if (!program.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Access denied");
        }

        List<ProgressLog> logs = progressLogRepository.findByWorkoutProgramIdOrderByDateDesc(programId);
        return logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ExerciseProgress createExerciseProgress(ExerciseProgressDto dto, ProgressLog progressLog) {
        ExerciseProgress progress = new ExerciseProgress();
        progress.setProgressLog(progressLog);

        if (dto.getWorkoutDayExerciseId() != null) {
            WorkoutDayExercise plannedExercise = workoutDayExerciseRepository
                    .findById(dto.getWorkoutDayExerciseId())
                    .orElseThrow(() -> new IllegalArgumentException("Planned exercise not found"));
            progress.setPlannedExercise(plannedExercise);
        }

        if (dto.getReplacementExerciseId() != null) {
            Exercise replacementExercise = exerciseRepository
                    .findById(dto.getReplacementExerciseId())
                    .orElseThrow(() -> new IllegalArgumentException("Replacement exercise not found"));
            progress.setReplacementExercise(replacementExercise);
        }

        progress.setOrderIndex(dto.getOrderIndex());
        progress.setActualSets(dto.getActualSets());

        // Add set details
        for (int i = 0; i < dto.getRepsPerSet().size(); i++) {
            progress.addSetDetail(dto.getRepsPerSet().get(i), dto.getWeightPerSet().get(i));
        }

        progress.setRestPeriodBetweenSets(dto.getRestPeriodBetweenSets());
        progress.setCompleted(dto.getCompleted());
        progress.setSkipped(dto.getSkipped());
        progress.setNotes(dto.getNotes());

        return progress;
    }

    private ProgressLogDto convertToDto(ProgressLog progressLog) {
        ProgressLogDto dto = new ProgressLogDto();
        dto.setId(progressLog.getId());
        dto.setDate(progressLog.getDate());
        dto.setNotes(progressLog.getNotes());
        dto.setWorkoutProgramId(progressLog.getWorkoutProgram().getId());
        dto.setWorkoutDayId(progressLog.getWorkoutDay().getId());
        dto.setWorkoutDayName(progressLog.getWorkoutDay().getDayOfWeek().name());

        List<ExerciseProgressDto> exerciseProgresses = progressLog.getExerciseProgresses().stream()
                .map(this::convertToExerciseProgressDto)
                .collect(Collectors.toList());

        dto.setExerciseProgresses(exerciseProgresses);
        return dto;
    }

    private ExerciseProgressDto convertToExerciseProgressDto(ExerciseProgress progress) {
        ExerciseProgressDto dto = new ExerciseProgressDto();
        dto.setId(progress.getId());

        if (progress.getPlannedExercise() != null) {
            dto.setWorkoutDayExerciseId(progress.getPlannedExercise().getId());
            dto.setExerciseName(progress.getPlannedExercise().getExercise().getName());
        }

        if (progress.getReplacementExercise() != null) {
            dto.setReplacementExerciseId(progress.getReplacementExercise().getId());
            dto.setExerciseName(progress.getReplacementExercise().getName());
        }

        dto.setOrderIndex(progress.getOrderIndex());
        dto.setActualSets(progress.getActualSets());

        // Add set details
        for (int i = 0; i < dto.getRepsPerSet().size(); i++) {
            progress.addSetDetail(dto.getRepsPerSet().get(i), dto.getWeightPerSet().get(i));
        }

        dto.setRestPeriodBetweenSets(progress.getRestPeriodBetweenSets());
        dto.setCompleted(progress.getCompleted());
        dto.setSkipped(progress.getSkipped());
        dto.setNotes(progress.getNotes());

        return dto;
    }

    private ProgressLogSummaryDto convertToSummaryDto(ProgressLog progressLog) {
        ProgressLogSummaryDto dto = new ProgressLogSummaryDto();
        dto.setId(progressLog.getId());
        dto.setDate(progressLog.getDate());
        dto.setWorkoutDayName(progressLog.getWorkoutDay().getDayOfWeek().name());

        int totalExercises = progressLog.getExerciseProgresses().size();
        int completedExercises = (int) progressLog.getExerciseProgresses().stream()
                .filter(ExerciseProgress::getCompleted)
                .count();
        int skippedExercises = (int) progressLog.getExerciseProgresses().stream()
                .filter(ExerciseProgress::getSkipped)
                .count();

        dto.setTotalExercises(totalExercises);
        dto.setCompletedExercises(completedExercises);
        dto.setSkippedExercises(skippedExercises);

        return dto;
    }
}
