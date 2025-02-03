package com.artisoft.fitbuddy.service;

import com.artisoft.fitbuddy.dto.AuthResponse;
import com.artisoft.fitbuddy.dto.LoginRequest;
import com.artisoft.fitbuddy.dto.SignUpRequest;
import com.artisoft.fitbuddy.model.User;
import com.artisoft.fitbuddy.repository.UserRepository;
import com.artisoft.fitbuddy.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ENCODED_PASSWORD = "encoded_password";
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_ROLE = "ROLE_USER";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Nested
    @DisplayName("signUp")
    class SignUp {
        private SignUpRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new SignUpRequest();
            validRequest.setUsername(TEST_USERNAME);
            validRequest.setEmail(TEST_EMAIL);
            validRequest.setPassword(TEST_PASSWORD);
        }

        @Test
        @DisplayName("should successfully create new user with valid input")
        void successfulSignUp() {
            // Given
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
            when(tokenProvider.generateToken(TEST_USERNAME)).thenReturn(TEST_TOKEN);
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(1L);
                return savedUser;
            });

            // When
            AuthResponse response = authService.signUp(validRequest);

            // Then
            assertNotNull(response);
            assertEquals(TEST_TOKEN, response.getToken());
            assertEquals(TEST_USERNAME, response.getUsername());
            assertEquals(TEST_ROLE, response.getRole());

            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertEquals(TEST_USERNAME, savedUser.getUsername());
            assertEquals(TEST_EMAIL, savedUser.getEmail());
            assertEquals(TEST_ENCODED_PASSWORD, savedUser.getPassword());
            assertEquals(TEST_ROLE, savedUser.getRole());
        }

        @Test
        @DisplayName("should throw exception when username already exists")
        void signUpWithExistingUsername() {
            // Given
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.signUp(validRequest)
            );
            assertEquals("Username is already taken", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void signUpWithExistingEmail() {
            // Given
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.signUp(validRequest)
            );
            assertEquals("Email is already registered", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login")
    class Login {
        private LoginRequest validRequest;
        private User existingUser;

        @BeforeEach
        void setUp() {
            validRequest = new LoginRequest();
            validRequest.setUsername(TEST_USERNAME);
            validRequest.setPassword(TEST_PASSWORD);

            existingUser = new User();
            existingUser.setId(1L);
            existingUser.setUsername(TEST_USERNAME);
            existingUser.setPassword(TEST_ENCODED_PASSWORD);
            existingUser.setRole(TEST_ROLE);
        }

        @Test
        @DisplayName("should successfully authenticate user with valid credentials")
        void successfulLogin() {
            // Given
            when(tokenProvider.generateToken(TEST_USERNAME)).thenReturn(TEST_TOKEN);
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(java.util.Optional.of(existingUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(TEST_USERNAME, TEST_PASSWORD));

            // When
            AuthResponse response = authService.login(validRequest);

            // Then
            assertNotNull(response);
            assertEquals(TEST_TOKEN, response.getToken());
            assertEquals(TEST_USERNAME, response.getUsername());
            assertEquals(TEST_ROLE, response.getRole());

            verify(authenticationManager).authenticate(
                    argThat(auth ->
                            auth.getPrincipal().equals(TEST_USERNAME) &&
                                    auth.getCredentials().equals(TEST_PASSWORD)
                    )
            );
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void loginWithNonexistentUser() {
            // Given
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(java.util.Optional.empty());

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.login(validRequest)
            );
            assertEquals("User not found", exception.getMessage());
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("should pass through authentication exception for invalid credentials")
        void loginWithInvalidCredentials() {
            // Given
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(java.util.Optional.of(existingUser));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

            // When/Then
            assertThrows(
                    org.springframework.security.authentication.BadCredentialsException.class,
                    () -> authService.login(validRequest)
            );
        }
    }
}
