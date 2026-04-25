package OMS_backend.service;

import OMS_backend.dto.request.LoginRequest;
import OMS_backend.dto.request.RegisterRequest;
import OMS_backend.dto.response.AuthResponse;
import OMS_backend.dto.response.UserResponse;
import OMS_backend.exception.DuplicateResourceException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.Role;
import OMS_backend.model.User;
import OMS_backend.repository.UserRepository;
import OMS_backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("Vansh Batham");
        testUser.setEmail("vansh@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.SALES_REPRESENTATIVE);

        registerRequest = new RegisterRequest();
        registerRequest.setName("Vansh Batham");
        registerRequest.setEmail("vansh@example.com");
        registerRequest.setPassword("secret123");
        registerRequest.setRole("SALES_REPRESENTATIVE");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("vansh@example.com");
        loginRequest.setPassword("secret123");
        loginRequest.setRole("SALES_REPRESENTATIVE");
    }

    // register
    @Test
    @DisplayName("Register - Success")
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.registerUser(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("vansh@example.com");
        assertThat(response.getName()).isEqualTo("Vansh Batham");
        assertThat(response.getRole()).isEqualTo("SALES_REPRESENTATIVE");

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    @DisplayName("Register - Duplicate Email throws DuplicateResourceException")
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("vansh@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register - Invalid Role throws IllegalArgumentException")
    void register_InvalidRole_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        registerRequest.setRole("INVALID_ROLE");

        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // login
    @Test
    @DisplayName("Login - Success returns AuthResponse with token")
    void login_Success() {
        UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        "vansh@example.com",
                        "encodedPassword",
                        List.of(new SimpleGrantedAuthority("SALES_REPRESENTATIVE"))
                );

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);
        when(userRepository.findByEmail("vansh@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("vansh@example.com"))
                .thenReturn(userDetails);
        when(jwtUtil.generateToken(any())).thenReturn("mock.jwt.token");

        AuthResponse response = userService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getEmail()).isEqualTo("vansh@example.com");
        assertThat(response.getRole()).isEqualTo("SALES_REPRESENTATIVE");
    }

    @Test
    @DisplayName("Login - Wrong role throws BadCredentialsException")
    void login_WrongRole_ThrowsException() {
        loginRequest.setRole("ADMINISTRATOR"); // user is SALES_REPRESENTATIVE

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("vansh@example.com"))
                .thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("not registered as ADMINISTRATOR");
    }

    @Test
    @DisplayName("Login - User not found throws ResourceNotFoundException")
    void login_UserNotFound_ThrowsException() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("vansh@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Login - Wrong password throws BadCredentialsException")
    void login_WrongPassword_ThrowsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ─── ADMIN OPERATIONS ─────────────────────────────────

    @Test
    @DisplayName("Get all users - Returns list")
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        var result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("vansh@example.com");
    }

    @Test
    @DisplayName("Get user by ID - Not found throws ResourceNotFoundException")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Delete user - Not found throws ResourceNotFoundException")
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}