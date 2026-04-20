package OMS_backend.service;

import OMS_backend.model.Product;
import OMS_backend.model.Role;
import OMS_backend.model.User;
import OMS_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryNotificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${app.notifications.email-enabled:false}")
    private boolean emailEnabled;

    @Value("${app.notifications.from:no-reply@oms.local}")
    private String fromEmail;

    public void notifyOutOfStock(Product product, int availableQuantity) {
        String subject = "OMS Stock Alert: " + product.getProductName() + " is out of stock";
        String body = "Product/component is out of stock.\n\n"
                + "Product ID: " + product.getProductId() + "\n"
                + "Product Name: " + product.getProductName() + "\n"
                + "Current Stock: " + availableQuantity + "\n\n"
                + "Please create a purchase order or replenish stock.";

        notifyPurchasingOfficers(subject, body);
    }

    public void notifyInsufficientStock(Product product, int requiredQuantity, int availableQuantity) {
        String subject = "OMS Stock Alert: Insufficient stock for " + product.getProductName();
        String body = "An order could not be fulfilled because stock is insufficient.\n\n"
                + "Product ID: " + product.getProductId() + "\n"
                + "Product Name: " + product.getProductName() + "\n"
                + "Required Quantity: " + requiredQuantity + "\n"
                + "Available Quantity: " + availableQuantity + "\n\n"
                + "Please create a purchase order or replenish stock.";

        notifyPurchasingOfficers(subject, body);
    }

    private void notifyPurchasingOfficers(String subject, String body) {

        List<User> purchasingOfficers = userRepository.findByRole(Role.PURCHASING_OFFICER);

        if (purchasingOfficers.isEmpty()) {
            log.warn("No purchasing officer users found for stock notification. subject={}", subject);
            return;
        }

        for (User officer : purchasingOfficers) {
            notificationService.createNotification(officer, subject, body);
        }

        if (!emailEnabled) {
            log.info("Email notifications are disabled. In-app notification created only. subject={}", subject);
            return;
        }

        for (User officer : purchasingOfficers) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(officer.getEmail());
                message.setSubject(subject);
                message.setText(body);

                mailSender.send(message);
                log.info("Stock notification email sent. recipient={}, subject={}",
                        officer.getEmail(), subject);
            } catch (Exception ex) {
                log.error("Failed to send stock notification email. recipient={}, subject={}",
                        officer.getEmail(), subject, ex);
            }
        }
    }
}
