package OMS_backend.dto.response;


import lombok.Data;

import java.util.List;

@Data
public class DashboardSummaryResponse {

    private long totalCustomers;
    private long totalBills;
    private List<MonthlyOrderDto> monthlyOrders;
}
