package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderSummaryReport {
    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long processingOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private double totalOrderValue;
}