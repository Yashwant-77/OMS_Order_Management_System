package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long invoiceId;
    private LocalDateTime paymentDate;
    private Double amount;
    private String paymentMethod;
    private String notes;
}