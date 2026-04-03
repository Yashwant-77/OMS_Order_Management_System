package OMS_backend.service;

import OMS_backend.dto.request.LoginRequest;
import OMS_backend.dto.request.RegisterRequest;
import OMS_backend.dto.response.AuthResponse;
import OMS_backend.model.Role;
import OMS_backend.model.User;
import OMS_backend.repository.UserRepository;
import OMS_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public User registerUser(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // ✅ BCrypt now
        user.setEmail(request.getEmail());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        // Step 1: Authenticate — throws BadCredentialsException if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Step 2: Load user for token generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Step 3: Generate JWT
        String token = jwtUtil.generateToken(userDetails);

        // Step 4: Fetch role to include in response
        User user = userRepository.findByUsername(request.getUsername()).get();

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}