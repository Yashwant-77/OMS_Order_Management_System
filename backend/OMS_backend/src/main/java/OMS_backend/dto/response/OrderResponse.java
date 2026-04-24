package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private Long salesOrderId;
    private Long customerId;
    private String customerName;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private List<OrderItemResponse> items;
}