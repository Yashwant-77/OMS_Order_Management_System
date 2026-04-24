package OMS_backend.config;

import OMS_backend.model.Role;
import OMS_backend.model.User;
import OMS_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // You can change these default credentials as needed
        String adminEmail = "chouhanyashwant98@gmail.com";
        String adminPassword = "Yashwant77@";

        // Check if our default admin already exists
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("No default admin found. Creating default admin user...");

            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMINISTRATOR);
            admin.setStatus("ACTIVE"); // Set to ACTIVE so they can log in immediately

            userRepository.save(admin);
            log.info("Default admin created successfully!");
            log.info("========================================");
            log.info("Email: {}", adminEmail);
            log.info("Password: {}", adminPassword);
            log.info("========================================");
        } else {
            log.info("Default admin user ({}) already exists.", adminEmail);
        }
    }
}
