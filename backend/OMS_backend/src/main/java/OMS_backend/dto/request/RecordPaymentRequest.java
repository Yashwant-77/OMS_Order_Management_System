package OMS_backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordPaymentRequest {
    private Long invoiceId;
    private Double amount;
    private String paymentMethod;
    private String notes;
}