package com.artisoft.fitbuddy.controller;

import com.artisoft.fitbuddy.config.TestSecurityConfig;
import com.artisoft.fitbuddy.dto.AuthResponse;
import com.artisoft.fitbuddy.dto.LoginRequest;
import com.artisoft.fitbuddy.dto.SignUpRequest;
import com.artisoft.fitbuddy.security.JwtTokenProvider;
import com.artisoft.fitbuddy.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable Spring Security filters for these tests
@ActiveProfiles("test")
class AuthControllerTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_ROLE = "ROLE_USER";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Nested
    @DisplayName("POST /api/auth/signup")
    class SignUp {
        private SignUpRequest validRequest;
        private AuthResponse successResponse;

        @BeforeEach
        void setUp() {
            validRequest = new SignUpRequest();
            validRequest.setUsername(TEST_USERNAME);
            validRequest.setEmail(TEST_EMAIL);
            validRequest.setPassword(TEST_PASSWORD);

            successResponse = new AuthResponse(TEST_TOKEN, TEST_USERNAME, TEST_ROLE);
        }

        @Test
        @DisplayName("should return 200 and token when signup is successful")
        void successfulSignUp() throws Exception {
            // Given
            when(authService.signUp(any(SignUpRequest.class))).thenReturn(successResponse);

            // When/Then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(TEST_TOKEN))
                    .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                    .andExpect(jsonPath("$.role").value(TEST_ROLE));
        }

        @Test
        @DisplayName("should return 400 when username is empty")
        void signUpWithEmptyUsername() throws Exception {
            // Given
            validRequest.setUsername("");

            // When/Then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when request is missing email")
        void signUpWithMissingEmail() throws Exception {
            // Given
            validRequest.setEmail(null);

            // When/Then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is too short")
        void signUpWithShortPassword() throws Exception {
            // Given
            validRequest.setPassword("123");

            // When/Then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when signup request has duplicate username")
        void signUpWithDuplicateUsername() throws Exception {
            // Given
            when(authService.signUp(any(SignUpRequest.class)))
                    .thenThrow(new IllegalArgumentException("Username is already taken"));

            // When/Then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {
        private LoginRequest validRequest;
        private AuthResponse successResponse;

        @BeforeEach
        void setUp() {
            validRequest = new LoginRequest();
            validRequest.setUsername(TEST_USERNAME);
            validRequest.setPassword(TEST_PASSWORD);

            successResponse = new AuthResponse(TEST_TOKEN, TEST_USERNAME, TEST_ROLE);
        }

        @Test
        @DisplayName("should return 200 and token when login is successful")
        void successfulLogin() throws Exception {
            // Given
            when(authService.login(any(LoginRequest.class))).thenReturn(successResponse);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(TEST_TOKEN))
                    .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                    .andExpect(jsonPath("$.role").value(TEST_ROLE));
        }

        @Test
        @DisplayName("should return 400 when username is missing")
        void loginWithMissingUsername() throws Exception {
            // Given
            validRequest.setUsername(null);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is missing")
        void loginWithMissingPassword() throws Exception {
            // Given
            validRequest.setPassword(null);

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when user not found")
        void loginWithNonExistentUser() throws Exception {
            // Given
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("User not found"));

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("User not found"));
        }

        @Test
        @DisplayName("should return 401 when password is invalid")
        void loginWithInvalidCredentials() throws Exception {
            // Given
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }
}