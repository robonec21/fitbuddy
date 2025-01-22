package com.artisoft.fitbuddy.controller;

import com.artisoft.fitbuddy.dto.ExerciseDto;
import com.artisoft.fitbuddy.dto.ExerciseSearchCriteria;
import com.artisoft.fitbuddy.dto.ExerciseUsageDto;
import com.artisoft.fitbuddy.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;

    @PostMapping
    public ResponseEntity<ExerciseDto> createExercise(
            @Valid @RequestBody ExerciseDto exerciseDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(exerciseService.createExercise(exerciseDto, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<ExerciseDto>> searchExercises(
            @ModelAttribute ExerciseSearchCriteria criteria,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(exerciseService.searchExercises(criteria, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDto> getExercise(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(exerciseService.getExercise(id, userDetails.getUsername()));
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<ExerciseUsageDto> getExerciseUsage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(exerciseService.getExerciseUsage(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExerciseDto> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseDto exerciseDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        exerciseDto.setId(id);
        return ResponseEntity.ok(exerciseService.updateExercise(id, exerciseDto, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        exerciseService.deleteExercise(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteExercises(
            @RequestParam List<Long> ids,
            @AuthenticationPrincipal UserDetails userDetails) {
        exerciseService.deleteExercises(ids, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
