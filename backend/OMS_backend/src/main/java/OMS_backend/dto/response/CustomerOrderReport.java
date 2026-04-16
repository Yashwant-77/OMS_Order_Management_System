package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerOrderReport {
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private long totalOrders;
    private double totalOrderValue;
}