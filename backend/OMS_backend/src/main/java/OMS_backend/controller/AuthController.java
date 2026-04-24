package OMS_backend.controller;

import OMS_backend.dto.request.LoginRequest;
import OMS_backend.dto.request.RegisterRequest;
import OMS_backend.dto.request.SetPasswordRequest;
import OMS_backend.dto.response.AuthResponse;
import OMS_backend.dto.response.UserResponse;
import OMS_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with a specified role")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login", description = "Authenticates user with email, password and role. Returns JWT token.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set Password", description = "Sets password for a new user using the token sent to their email")
    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        userService.setPassword(request);
        return ResponseEntity.ok("Password set successfully");
    }

    @Operation(summary = "Forgot Password", description = "Sends an OTP to the user's email if the role and email match")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody OMS_backend.dto.request.ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok("If the email and role match an active account, an OTP will be sent.");
    }

    @Operation(summary = "Reset Password with OTP", description = "Resets the password using the OTP sent to email")
    @PostMapping("/reset-password-otp")
    public ResponseEntity<String> resetPasswordWithOtp(@Valid @RequestBody OMS_backend.dto.request.ResetPasswordWithOtpRequest request) {
        userService.resetPasswordWithOtp(request);
        return ResponseEntity.ok("Password reset successfully");
    }
}