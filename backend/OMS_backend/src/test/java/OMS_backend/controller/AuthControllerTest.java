package OMS_backend.controller;

import OMS_backend.config.SecurityConfig;
import OMS_backend.dto.request.LoginRequest;
import OMS_backend.dto.request.RegisterRequest;
import OMS_backend.dto.response.AuthResponse;
import OMS_backend.dto.response.UserResponse;
import OMS_backend.exception.GlobalExceptionHandler;
import OMS_backend.security.JwtUtil;
import OMS_backend.service.CustomUserDetailsService;
import OMS_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Vansh Batham");
        registerRequest.setEmail("vansh@example.com");
        registerRequest.setPassword("secret123");
        registerRequest.setRole("SALES_REPRESENTATIVE");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("vansh@example.com");
        loginRequest.setPassword("secret123");
        loginRequest.setRole("SALES_REPRESENTATIVE");

        userResponse = new UserResponse(
                1L, "Vansh Batham",
                "vansh@example.com", "SALES_REPRESENTATIVE");

        authResponse = new AuthResponse(
                "mock.jwt.token", "Vansh Batham",
                "vansh@example.com", "SALES_REPRESENTATIVE");
    }

    @Test
    @DisplayName("POST /api/auth/register - Success returns 200")
    void register_Success_Returns200() throws Exception {
        when(userService.registerUser(any(RegisterRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("vansh@example.com"))
                .andExpect(jsonPath("$.name")
                        .value("Vansh Batham"))
                .andExpect(jsonPath("$.role")
                        .value("SALES_REPRESENTATIVE"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Missing fields returns 400")
    void register_MissingFields_Returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Success returns token")
    void login_Success_ReturnsToken() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("mock.jwt.token"))
                .andExpect(jsonPath("$.email")
                        .value("vansh@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Wrong credentials returns 401")
    void login_WrongCredentials_Returns401() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/login - Invalid email format returns 400")
    void login_InvalidEmail_Returns400() throws Exception {
        loginRequest.setEmail("notanemail");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));
    }
}