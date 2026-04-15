package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InvoiceResponse {
    private Long invoiceId;
    private Long salesOrderId;
    private String customerName;
    private LocalDateTime invoiceDate;
    private Double totalAmount;
    private Double paidAmount;
    private Double outstandingAmount;
    private String status;
}