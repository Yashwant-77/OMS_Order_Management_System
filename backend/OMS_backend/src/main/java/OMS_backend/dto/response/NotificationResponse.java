package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private String title;
    private String message;
    private boolean readStatus;
    private LocalDateTime createdAt;
}
