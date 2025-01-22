package com.artisoft.fitbuddy.controller;

import com.artisoft.fitbuddy.dto.WorkoutDayDto;
import com.artisoft.fitbuddy.dto.WorkoutProgramDto;
import com.artisoft.fitbuddy.dto.WorkoutProgramWithLogsDto;
import com.artisoft.fitbuddy.service.WorkoutProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workout-programs")
@RequiredArgsConstructor
public class WorkoutProgramController {
//    private final UserService userService;
    private final WorkoutProgramService workoutProgramService;

    @PostMapping
    public ResponseEntity<WorkoutProgramDto> createWorkoutProgram(
            @Valid @RequestBody WorkoutProgramDto workoutProgramDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workoutProgramService.createWorkoutProgram(workoutProgramDto, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<WorkoutProgramDto>> getUserWorkoutPrograms(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workoutProgramService.getUserWorkoutPrograms(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutProgramDto> getWorkoutProgram(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workoutProgramService.getWorkoutProgram(id, userDetails.getUsername()));
    }

    @PutMapping("/{programId}/days/{dayId}")
    public ResponseEntity<WorkoutProgramDto> updateWorkoutDay(
            @PathVariable Long programId,
            @PathVariable Long dayId,
            @Valid @RequestBody WorkoutDayDto workoutDayDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workoutProgramService.updateProgramWorkoutDay(programId, dayId, workoutDayDto, userDetails.getUsername()));
    }

    @GetMapping("/with-logs")
    public ResponseEntity<List<WorkoutProgramWithLogsDto>> getWorkoutProgramsWithLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workoutProgramService.getWorkoutProgramsWithLogs(userDetails.getUsername()));
    }

    @GetMapping("/without-logs")
    public ResponseEntity<List<WorkoutProgramDto>> getWorkoutProgramsWithoutLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(workoutProgramService.getWorkoutProgramsWithoutLogs(userDetails.getUsername()));
    }
}
