package OMS_backend.controller;

import OMS_backend.dto.request.RegisterRequest;
import OMS_backend.model.User;
import OMS_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        return ResponseEntity.ok("User registered successfully: " + user.getUsername());
    }

    // POST /api/auth/login → comes in next commit (JWT)
}