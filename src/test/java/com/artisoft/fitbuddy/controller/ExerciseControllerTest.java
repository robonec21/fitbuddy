package com.artisoft.fitbuddy.controller;

import com.artisoft.fitbuddy.config.TestSecurityConfig;
import com.artisoft.fitbuddy.dto.ExerciseDto;
import com.artisoft.fitbuddy.dto.ExerciseSearchCriteria;
import com.artisoft.fitbuddy.dto.ExerciseUsageDto;
import com.artisoft.fitbuddy.security.JwtTokenProvider;
import com.artisoft.fitbuddy.service.ExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.trilead.ssh2.auth.AuthenticationManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ExerciseController.class,
        properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExerciseService exerciseService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private static final String TEST_USERNAME = "testuser";
    private static final Long TEST_EXERCISE_ID = 1L;
    private static final String TEST_EXERCISE_NAME = "Barbell Squat";
    private static final String TEST_EXERCISE_DESC = "A compound leg exercise";

    @Nested
    @DisplayName("GET /api/exercises")
    class SearchExercises {
        private ExerciseDto exercise1;
        private ExerciseDto exercise2;
        private List<ExerciseDto> exerciseList;

        @BeforeEach
        void setUp() {
            exercise1 = new ExerciseDto();
            exercise1.setId(1L);
            exercise1.setName("Squat");
            exercise1.setDefaultSets(3);
            exercise1.setDefaultRepsPerSet(10);

            exercise2 = new ExerciseDto();
            exercise2.setId(2L);
            exercise2.setName("Deadlift");
            exercise2.setDefaultSets(3);
            exercise2.setDefaultRepsPerSet(8);

            exerciseList = Arrays.asList(exercise1, exercise2);
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return matching exercises when search criteria provided")
        void searchExercisesWithCriteria() throws Exception {
            when(exerciseService.searchExercises(any(ExerciseSearchCriteria.class), eq(TEST_USERNAME)))
                    .thenReturn(exerciseList);

            mockMvc.perform(get("/api/exercises")
                            .param("name", "Squat"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").value(exercise1.getName()))
                    .andExpect(jsonPath("$[1].name").value(exercise2.getName()));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return empty list when no exercises match criteria")
        void searchExercisesNoMatches() throws Exception {
            when(exerciseService.searchExercises(any(ExerciseSearchCriteria.class), eq(TEST_USERNAME)))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/exercises")
                            .param("name", "NonExistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should return 401 when user is not authenticated")
        void searchExercisesUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/exercises"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/exercises")
    class CreateExercise {
        private ExerciseDto validExercise;

        @BeforeEach
        void setUp() {
            validExercise = new ExerciseDto();
            validExercise.setName(TEST_EXERCISE_NAME);
            validExercise.setDescription(TEST_EXERCISE_DESC);
            validExercise.setDefaultSets(3);
            validExercise.setDefaultRepsPerSet(10);
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should create exercise with valid data")
        void createValidExercise() throws Exception {
            when(exerciseService.createExercise(any(ExerciseDto.class), eq(TEST_USERNAME)))
                    .thenReturn(validExercise);

            mockMvc.perform(post("/api/exercises")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validExercise)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(TEST_EXERCISE_NAME))
                    .andExpect(jsonPath("$.description").value(TEST_EXERCISE_DESC));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when exercise name is missing")
        void createExerciseWithoutName() throws Exception {
            validExercise.setName(null);

            mockMvc.perform(post("/api/exercises")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validExercise)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when exercise sets is invalid")
        void createExerciseWithInvalidSets() throws Exception {
            validExercise.setDefaultSets(0);

            mockMvc.perform(post("/api/exercises")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validExercise)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when user is not found")
        void createExerciseWithNonExistentUser() throws Exception {
            when(exerciseService.createExercise(any(ExerciseDto.class), eq(TEST_USERNAME)))
                    .thenThrow(new IllegalArgumentException("User not found"));

            mockMvc.perform(post("/api/exercises")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validExercise)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("User not found"));
        }
    }

    @Nested
    @DisplayName("GET /api/exercises/{id}")
    class GetExercise {
        private ExerciseDto exercise;

        @BeforeEach
        void setUp() {
            exercise = new ExerciseDto();
            exercise.setId(TEST_EXERCISE_ID);
            exercise.setName(TEST_EXERCISE_NAME);
            exercise.setDescription(TEST_EXERCISE_DESC);
            exercise.setDefaultSets(3);
            exercise.setDefaultRepsPerSet(10);
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return exercise when found")
        void getExistingExercise() throws Exception {
            when(exerciseService.getExercise(TEST_EXERCISE_ID, TEST_USERNAME))
                    .thenReturn(exercise);

            mockMvc.perform(get("/api/exercises/{id}", TEST_EXERCISE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TEST_EXERCISE_ID))
                    .andExpect(jsonPath("$.name").value(TEST_EXERCISE_NAME));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 404 when exercise not found")
        void getNonExistentExercise() throws Exception {
            when(exerciseService.getExercise(TEST_EXERCISE_ID, TEST_USERNAME))
                    .thenThrow(new IllegalArgumentException("Exercise not found"));

            mockMvc.perform(get("/api/exercises/{id}", TEST_EXERCISE_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Exercise not found"));
        }
    }

    @Nested
    @DisplayName("PUT /api/exercises/{id}")
    class UpdateExercise {
        private ExerciseDto exerciseUpdate;

        @BeforeEach
        void setUp() {
            exerciseUpdate = new ExerciseDto();
            exerciseUpdate.setId(TEST_EXERCISE_ID);
            exerciseUpdate.setName("Updated " + TEST_EXERCISE_NAME);
            exerciseUpdate.setDescription("Updated " + TEST_EXERCISE_DESC);
            exerciseUpdate.setDefaultSets(4);
            exerciseUpdate.setDefaultRepsPerSet(12);
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should update exercise with valid data")
        void updateValidExercise() throws Exception {
            when(exerciseService.updateExercise(eq(TEST_EXERCISE_ID), any(ExerciseDto.class), eq(TEST_USERNAME)))
                    .thenReturn(exerciseUpdate);

            mockMvc.perform(put("/api/exercises/{id}", TEST_EXERCISE_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(exerciseUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(exerciseUpdate.getName()))
                    .andExpect(jsonPath("$.description").value(exerciseUpdate.getDescription()));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when updated exercise data is invalid")
        void updateExerciseWithInvalidData() throws Exception {
            exerciseUpdate.setDefaultSets(-1);

            mockMvc.perform(put("/api/exercises/{id}", TEST_EXERCISE_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(exerciseUpdate)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when exercise is not found")
        void updateNonExistentExercise() throws Exception {
            when(exerciseService.updateExercise(eq(TEST_EXERCISE_ID), any(ExerciseDto.class), eq(TEST_USERNAME)))
                    .thenThrow(new IllegalArgumentException("Exercise not found"));

            mockMvc.perform(put("/api/exercises/{id}", TEST_EXERCISE_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(exerciseUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Exercise not found"));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when user lacks access to exercise")
        void updateExerciseWithoutAccess() throws Exception {
            when(exerciseService.updateExercise(eq(TEST_EXERCISE_ID), any(ExerciseDto.class), eq(TEST_USERNAME)))
                    .thenThrow(new IllegalArgumentException("Access denied"));

            mockMvc.perform(put("/api/exercises/{id}", TEST_EXERCISE_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(exerciseUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Access denied"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/exercises/{id}")
    class DeleteExercise {
        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should successfully delete existing exercise")
        void deleteExistingExercise() throws Exception {
            mockMvc.perform(delete("/api/exercises/{id}", TEST_EXERCISE_ID).with(csrf()))
                    .andExpect(status().isNoContent());

            verify(exerciseService).deleteExercise(TEST_EXERCISE_ID, TEST_USERNAME);
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when exercise is in use")
        void deleteExerciseInUse() throws Exception {
            org.mockito.Mockito.doThrow(new IllegalArgumentException("Exercise cannot be deleted"))
                    .when(exerciseService).deleteExercise(TEST_EXERCISE_ID, TEST_USERNAME);

            mockMvc.perform(delete("/api/exercises/{id}", TEST_EXERCISE_ID).with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Exercise cannot be deleted"));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when exercise not found")
        void deleteNonExistentExercise() throws Exception {
            org.mockito.Mockito.doThrow(new IllegalArgumentException("Exercise not found"))
                    .when(exerciseService).deleteExercise(TEST_EXERCISE_ID, TEST_USERNAME);

            mockMvc.perform(delete("/api/exercises/{id}", TEST_EXERCISE_ID).with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Exercise not found"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/exercises (Bulk Delete)")
    class DeleteExercises {
        private List<Long> exerciseIds;

        @BeforeEach
        void setUp() {
            exerciseIds = Arrays.asList(1L, 2L, 3L);
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should successfully delete multiple exercises")
        void deleteMultipleExercises() throws Exception {
            mockMvc.perform(delete("/api/exercises")
                            .with(csrf())
                            .param("ids", "1", "2", "3"))
                    .andExpect(status().isNoContent());

            verify(exerciseService).deleteExercises(exerciseIds, TEST_USERNAME);
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when any exercise is in use")
        void deleteExercisesWithOneInUse() throws Exception {
            org.mockito.Mockito.doThrow(new IllegalArgumentException("Exercise with ID 2 cannot be deleted"))
                    .when(exerciseService).deleteExercises(exerciseIds, TEST_USERNAME);

            mockMvc.perform(delete("/api/exercises")
                            .with(csrf())
                            .param("ids", "1", "2", "3"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Exercise with ID 2 cannot be deleted"));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when any exercise is not found")
        void deleteExercisesWithOneNotFound() throws Exception {
            org.mockito.Mockito.doThrow(new IllegalArgumentException("Exercise with ID 3 not found"))
                    .when(exerciseService).deleteExercises(exerciseIds, TEST_USERNAME);

            mockMvc.perform(delete("/api/exercises")
                            .with(csrf())
                            .param("ids", "1", "2", "3"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Exercise with ID 3 not found"));
        }

        @Test
        @DisplayName("should return 401 when user is not authenticated")
        void deleteExercisesUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/exercises")
                            .param("ids", "1", "2", "3"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 400 when no IDs provided")
        void deleteExercisesWithNoIds() throws Exception {
            mockMvc.perform(delete("/api/exercises").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/exercises/{id}/usage")
    class GetExerciseUsage {
        private ExerciseUsageDto usageInfo;

        @BeforeEach
        void setUp() {
            usageInfo = new ExerciseUsageDto();
            usageInfo.setExerciseId(TEST_EXERCISE_ID);
            usageInfo.setExerciseName(TEST_EXERCISE_NAME);

            ExerciseUsageDto.WorkoutProgramUsageDto programUsage = new ExerciseUsageDto.WorkoutProgramUsageDto();
            programUsage.setProgramId(1L);
            programUsage.setProgramName("Test Program");

            ExerciseUsageDto.WorkoutDayUsageDto dayUsage = new ExerciseUsageDto.WorkoutDayUsageDto();
            dayUsage.setDayId(1L);
            dayUsage.setDayOfWeek("MONDAY");

            programUsage.setDays(Collections.singletonList(dayUsage));
            usageInfo.setPrograms(Collections.singletonList(programUsage));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return usage info for existing exercise")
        void getExistingExerciseUsage() throws Exception {
            when(exerciseService.getExerciseUsage(TEST_EXERCISE_ID, TEST_USERNAME))
                    .thenReturn(usageInfo);

            mockMvc.perform(get("/api/exercises/{id}/usage", TEST_EXERCISE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exerciseId").value(TEST_EXERCISE_ID))
                    .andExpect(jsonPath("$.exerciseName").value(TEST_EXERCISE_NAME))
                    .andExpect(jsonPath("$.programs", hasSize(1)))
                    .andExpect(jsonPath("$.programs[0].days", hasSize(1)));
        }

        @Test
        @WithMockUser(username = TEST_USERNAME)
        @DisplayName("should return 404 when exercise not found for usage info")
        void getNonExistentExerciseUsage() throws Exception {
            when(exerciseService.getExerciseUsage(TEST_EXERCISE_ID, TEST_USERNAME))
                    .thenThrow(new IllegalArgumentException("Exercise not found"));

            mockMvc.perform(get("/api/exercises/{id}/usage", TEST_EXERCISE_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Exercise not found"));
        }
    }
}