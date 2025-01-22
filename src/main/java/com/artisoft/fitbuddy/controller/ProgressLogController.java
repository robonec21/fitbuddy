package com.artisoft.fitbuddy.controller;

import com.artisoft.fitbuddy.dto.ProgressLogDto;
import com.artisoft.fitbuddy.dto.ProgressLogSummaryDto;
import com.artisoft.fitbuddy.service.ProgressLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/progress-logs")
@RequiredArgsConstructor
public class ProgressLogController {
    private final ProgressLogService progressLogService;

    @PostMapping
    public ResponseEntity<ProgressLogDto> createProgressLog(
            @Valid @RequestBody ProgressLogDto progressLogDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(progressLogService.createProgressLog(progressLogDto, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<ProgressLogSummaryDto>> getProgressLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(progressLogService.getUserProgressLogs(userDetails.getUsername(), startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgressLogDto> getProgressLog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(progressLogService.getProgressLog(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgressLogDto> updateProgressLog(
            @PathVariable Long id,
            @Valid @RequestBody ProgressLogDto progressLogDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(progressLogService.updateProgressLog(id, progressLogDto, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgressLog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        progressLogService.deleteProgressLog(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/program/{programId}/summaries")
    public ResponseEntity<List<ProgressLogSummaryDto>> getProgramLogSummaries(
            @PathVariable Long programId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(progressLogService.getProgressLogSummaries(programId, userDetails.getUsername()));
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<ProgressLogDto>> getProgramLogs(
            @PathVariable Long programId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(progressLogService.getProgressLogs(programId, userDetails.getUsername()));
    }
}
