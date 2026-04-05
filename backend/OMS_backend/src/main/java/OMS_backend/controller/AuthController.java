package OMS_backend.controller;

import OMS_backend.dto.request.LoginRequest;
import OMS_backend.dto.request.RegisterRequest;
import OMS_backend.dto.response.AuthResponse;
import OMS_backend.model.User;
import OMS_backend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("api/auth")
@AllArgsConstructor
public class AuthController {
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        return new ResponseEntity<>("User registered successfully: " + user.getUsername(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
