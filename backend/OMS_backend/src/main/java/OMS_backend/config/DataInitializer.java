package OMS_backend.config;

import OMS_backend.model.Role;
import OMS_backend.model.User;
import OMS_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {

            String adminEmail = "yash@gmail.com";

            // ✅ Check if admin already exists
            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("yash123")); // 🔐 encoded
                admin.setRole(Role.ADMINISTRATOR);

                userRepository.save(admin);

                System.out.println("✅ Admin user created successfully!");
            } else {
                System.out.println("⚡ Admin already exists");
            }
        };
    }
}