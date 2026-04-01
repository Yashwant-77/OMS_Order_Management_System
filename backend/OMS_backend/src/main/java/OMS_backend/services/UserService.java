package OMS_backend.services;

import OMS_backend.DTO.RegisterRequest;
import OMS_backend.contoller.Role;
import OMS_backend.model.User;
import OMS_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(RegisterRequest request) {

        if (userRepository.findByUsername(request.username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username);
        user.setPassword(request.password); // ⚠️ later use BCrypt
        user.setEmail(request.email);
        user.setRole(Role.valueOf(request.role));

        return userRepository.save(user);
    }
}