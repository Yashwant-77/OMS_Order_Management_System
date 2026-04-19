package OMS_backend.service;


import OMS_backend.dto.response.DashboardSummaryResponse;
import OMS_backend.dto.response.MonthlyOrderDto;
import OMS_backend.repository.CustomerRepository;
import OMS_backend.repository.InvoiceRepository;
import OMS_backend.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final SalesOrderRepository salesOrderRepository;

    public DashboardSummaryResponse getDashboardSummary() {

        long totalCustomers = customerRepository.count();
        long totalBills = invoiceRepository.count();

        List<Object[]> monthlyData = salesOrderRepository.getMonthlyOrders();

        List<MonthlyOrderDto> monthlyOrders = new ArrayList<>();

        for (Object[] row : monthlyData) {
            System.out.println("Month: " + row[0] + " Count: " + row[1]);
            int monthNumber = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();

            String monthName = Month.of(monthNumber)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            monthlyOrders.add(new MonthlyOrderDto(monthName, count));
        }

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalCustomers(totalCustomers);
        response.setTotalBills(totalBills);
        response.setMonthlyOrders(monthlyOrders);

        return response;
    }
}
