package com.artisoft.fitbuddy.controller;

import com.artisoft.fitbuddy.dto.WorkoutProgramDto;
import com.artisoft.fitbuddy.service.WorkoutProgramService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutProgramController.class)
class WorkoutProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkoutProgramService workoutProgramService;

    @Test
    @WithMockUser(username = "testuser")
    void createWorkoutProgram_ValidProgram_ReturnsCreatedProgram() throws Exception {
        // Given
        WorkoutProgramDto request = createSampleWorkoutProgramDto();
        when(workoutProgramService.createWorkoutProgram(any(WorkoutProgramDto.class), eq("testuser")))
                .thenReturn(request);

        // When & Then
        mockMvc.perform(post("/api/workout-programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserWorkoutPrograms_ReturnsListOfPrograms() throws Exception {
        // Given
        List<WorkoutProgramDto> programs = Arrays.asList(
                createSampleWorkoutProgramDto(),
                createSampleWorkoutProgramDto()
        );
        when(workoutProgramService.getUserWorkoutPrograms("testuser")).thenReturn(programs);

        // When & Then
        mockMvc.perform(get("/api/workout-programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getWorkoutPrograms_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/workout-programs"))
                .andExpect(status().isUnauthorized());
    }

    private WorkoutProgramDto createSampleWorkoutProgramDto() {
        WorkoutProgramDto dto = new WorkoutProgramDto();
        dto.setId(1L);
        dto.setName("Sample Program");
        dto.setDescription("A sample workout program");
        return dto;
    }
}
