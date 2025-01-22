package com.artisoft.fitbuddy.controller;

import com.artisoft.fitbuddy.dto.ExerciseDto;
import com.artisoft.fitbuddy.dto.ExerciseSearchCriteria;
import com.artisoft.fitbuddy.service.ExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExerciseController.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExerciseService exerciseService;

    @Test
    @WithMockUser(username = "testuser")
    void createExercise_ValidExercise_ReturnsCreatedExercise() throws Exception {
        // Given
        ExerciseDto request = createSampleExerciseDto();
        when(exerciseService.createExercise(any(ExerciseDto.class), eq("testuser")))
                .thenReturn(request);

        // When & Then
        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchExercises_ReturnsMatchingExercises() throws Exception {
        // Given
        ExerciseDto exercise = createSampleExerciseDto();
        when(exerciseService.searchExercises(any(ExerciseSearchCriteria.class), eq("testuser")))
                .thenReturn(Arrays.asList(exercise));

        // When & Then
        mockMvc.perform(get("/api/exercises")
                        .param("name", "Squat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(exercise.getName()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateExercise_ValidUpdate_ReturnsUpdatedExercise() throws Exception {
        // Given
        ExerciseDto request = createSampleExerciseDto();
        when(exerciseService.updateExercise(eq(1L), any(ExerciseDto.class), eq("testuser")))
                .thenReturn(request);

        // When & Then
        mockMvc.perform(put("/api/exercises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    private ExerciseDto createSampleExerciseDto() {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(1L);
        dto.setName("Squat");
        dto.setDescription("Basic squat exercise");
        dto.setDefaultSets(3);
        dto.setDefaultRepsPerSet(10);
        return dto;
    }
}
