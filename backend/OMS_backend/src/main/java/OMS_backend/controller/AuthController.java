package OMS_backend.controller;

import OMS_backend.dto.request.RegisterRequest;
import OMS_backend.model.User;
import OMS_backend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@AllArgsConstructor
public class AuthController {


    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/login"){
        public ResponseEntity<?> login(@RequestBody LoginRequest request){

        }
    }
}
