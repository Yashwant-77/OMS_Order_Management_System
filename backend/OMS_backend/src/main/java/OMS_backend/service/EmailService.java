package OMS_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendSetPasswordEmail(String to, String name, String token) {
        log.info("Sending set password email to {}", to);
        try {
            // URL format depends on frontend routing. 
            // The frontend is usually hosted on localhost:4200 during dev
            String resetUrl = "http://localhost:4200/set-password?token=" + token;

            log.info("===================================================================");
            log.info("ATTENTION: User needs to set their password using the following link:");
            log.info(resetUrl);
            log.info("===================================================================");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Set Your Account Password");
            message.setText("Hello " + name + ",\n\n" +
                    "Your account has been created successfully. " +
                    "Please click on the following link to set your password:\n\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "Regards,\n" +
                    "Order Management System");
            
            mailSender.send(message);
            log.info("Set password email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send set password email to {}. Reason: {}", to, e.getMessage());
        }
    }
}
