package OMS_backend.service;

import OMS_backend.dto.request.LoginRequest;
import OMS_backend.dto.request.RegisterRequest;
import OMS_backend.dto.request.UpdateUserRequest;
import OMS_backend.dto.response.AuthResponse;
import OMS_backend.dto.response.UserResponse;
import OMS_backend.exception.DuplicateResourceException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.Role;
import OMS_backend.model.User;
import OMS_backend.repository.UserRepository;
import OMS_backend.repository.PasswordResetTokenRepository;
import OMS_backend.model.PasswordResetToken;
import OMS_backend.dto.request.CreateUserRequest;
import OMS_backend.dto.request.SetPasswordRequest;
import OMS_backend.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    // registration
    public UserResponse registerUser(RegisterRequest request) {

        log.info("Registering user. email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists. email={}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid role provided during registration. role={}", request.getRole());
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        log.info("User registered successfully. userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        return mapToResponse(savedUser);
    }

    // create user by admin
    public UserResponse createUserByAdmin(CreateUserRequest request) {
        log.info("Admin creating user. email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Creation failed - email already exists. email={}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid role provided during creation. role={}", request.getRole());
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // dummy password
        user.setRole(role);
        user.setStatus("PENDING");

        User savedUser = userRepository.save(user);

        // Generate token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(savedUser);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(resetToken);

        // Send email
        emailService.sendSetPasswordEmail(savedUser.getEmail(), savedUser.getName(), token);

        log.info("User created successfully by admin. userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        return mapToResponse(savedUser);
    }

    @Transactional
    public void setPassword(SetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        userRepository.save(user);

        tokenRepository.delete(resetToken);
        log.info("Password set successfully for user. email={}", user.getEmail());
    }

    // login
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt. email={}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            log.warn("Authentication failed. email={}", request.getEmail());
            throw ex;
        }

        // load user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found during login. email={}", request.getEmail());
                    return new ResourceNotFoundException("User not found");
                });

        // validate role
        Role selectedRole;
        try {
            selectedRole = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid role provided during login. role={}", request.getRole());
            throw new BadCredentialsException("Invalid role: " + request.getRole());
        }

        if (!user.getRole().equals(selectedRole)) {
            log.warn("Role mismatch. email={}, expected={}, provided={}",
                    request.getEmail(), user.getRole(), selectedRole);
            throw new BadCredentialsException("You are not registered as " + selectedRole.name());
        }

        // generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        log.info("Login successful. email={}, role={}", user.getEmail(), user.getRole());

        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // admin operations
    public List<UserResponse> getAllUsers() {

        log.info("Fetching all users");

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} users", users.size());

        return users;
    }

    public UserResponse getUserById(Long id) {

        log.info("Fetching user by id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found. id={}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });

        return mapToResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        log.info("Updating user. id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found for update. id={}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });

        if (request.getName() != null) {
            log.debug("Updating name for userId={}", id);
            user.setName(request.getName());
        }

        if (request.getEmail() != null) {
            log.debug("Updating email for userId={}", id);
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null) {
            try {
                Role newRole = Role.valueOf(request.getRole().toUpperCase());
                log.debug("Updating role for userId={} to {}", id, newRole);
                user.setRole(newRole);
            } catch (IllegalArgumentException ex) {
                log.error("Invalid role provided during update. role={}", request.getRole());
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }
        }

        User updatedUser = userRepository.save(user);

        log.info("User updated successfully. id={}", updatedUser.getUserId());

        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {

        log.info("Deleting user. id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found for deletion. id={}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });

        // Delete the associated password reset token first to prevent foreign key violation
        tokenRepository.deleteByUser(user);

        userRepository.deleteById(id);

        log.info("User deleted successfully. id={}", id);
    }

    private UserResponse mapToResponse(User user) {
        log.debug("Mapping User to UserResponse. userId={}", user.getUserId());

        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus() != null ? user.getStatus() : "ACTIVE"
        );
    }
}