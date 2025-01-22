package com.artisoft.fitbuddy.controller;

import com.artisoft.fitbuddy.dto.ProgressLogDto;
import com.artisoft.fitbuddy.service.ProgressLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressLogController.class)
class ProgressLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProgressLogService progressLogService;

    @Test
    @WithMockUser(username = "testuser")
    void createProgressLog_ValidLog_ReturnsCreatedLog() throws Exception {
        // Given
        ProgressLogDto request = createSampleProgressLogDto();
        when(progressLogService.createProgressLog(any(ProgressLogDto.class), eq("testuser")))
                .thenReturn(request);

        // When & Then
        mockMvc.perform(post("/api/progress-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.getId()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getProgressLog_ReturnsLog() throws Exception {
        // Given
        ProgressLogDto log = createSampleProgressLogDto();
        when(progressLogService.getProgressLog(1L, eq("testuser")))
                .thenReturn(log);

        // When & Then
        mockMvc.perform(get("/api/progress-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(log.getId()))
                .andExpect(jsonPath("$.date").value(log.getDate()))
                .andExpect(jsonPath("$.notes").value(log.getNotes()))
                .andExpect(jsonPath("$.workoutProgramId").value(log.getWorkoutProgramId()))
                .andExpect(jsonPath("$.workoutDayId").value(log.getWorkoutDayId()))
                .andExpect(jsonPath("$.workoutDayName").value(log.getWorkoutDayName()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateProgressLog_ValidUpdate_ReturnsUpdatedLog() throws Exception {
        // Given
        ProgressLogDto request = createSampleProgressLogDto();
        when(progressLogService.updateProgressLog(eq(1L), any(ProgressLogDto.class), eq("testuser")))
                .thenReturn(request);

        // When & Then
        mockMvc.perform(put("/api/progress-logs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.getId()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteProgressLog_ExistingLog_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/progress-logs/1"))
                .andExpect(status().isNoContent());
    }

    private ProgressLogDto createSampleProgressLogDto() {
        ProgressLogDto dto = new ProgressLogDto();
        dto.setId(1L);
        dto.setDate(LocalDate.now());
        dto.setWorkoutProgramId(1L);
        dto.setWorkoutDayId(1L);
        dto.setWorkoutDayName("MONDAY");
        dto.setNotes("Great workout!");
        return dto;
    }
}
