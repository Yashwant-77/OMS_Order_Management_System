package OMS_backend.dto.response;


import lombok.Data;

import java.util.List;

@Data
public class DashboardSummaryResponse {

    private long totalCustomers;
    private long totalBills;
    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long processingOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private double totalOrderValue;
    private List<MonthlyOrderDto> monthlyOrders;
}
