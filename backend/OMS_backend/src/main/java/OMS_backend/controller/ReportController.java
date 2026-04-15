package OMS_backend.controller;

import OMS_backend.dto.response.*;
import OMS_backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reporting & Analytics", description = "Business intelligence reports and dashboards")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Order summary report",
            description = "Returns order counts grouped by status " +
                    "and total order value." + "BUSINESS_ANALYST, ADMINISTRATOR or SALES_REPRESENTATIVE.")
    @GetMapping("/orders/summary")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'SALES_REPRESENTATIVE')")
    public ResponseEntity<OrderSummaryReport> getOrderSummary() {
        return new ResponseEntity<>(reportService.getOrderSummary(), HttpStatus.OK);
    }

    @Operation(summary = "Revenue report", description = "Returns total revenue and revenue by product category. " +
            "BUSINESS_ANALYST, ADMINISTRATOR or FINANCIAL_MANAGER.")
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'FINANCIAL_MANAGER')")
    public ResponseEntity<RevenueReport> getRevenueReport() {
        return new ResponseEntity<>(reportService.getRevenueReport(), HttpStatus.OK);
    }

    @Operation(summary = "Low stock report", description = "Returns products at or below the stock threshold." +
            " Default threshold is 10. " +
            "BUSINESS_ANALYST, ADMINISTRATOR, PRODUCT_MANAGER or PURCHASING_OFFICER.")
    @GetMapping("/products/low-stock")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'PRODUCT_MANAGER', 'PURCHASING_OFFICER')")
    public ResponseEntity<List<LowStockReport>> getLowStockReport(@RequestParam(defaultValue = "10") int threshold) {
        return new ResponseEntity<>(reportService.getLowStockReport(threshold), HttpStatus.OK);
    }

    @Operation(summary = "Customer order report", description = "Returns total orders and revenue by customer. " +
            "BUSINESS_ANALYST, ADMINISTRATOR or SALES_REPRESENTATIVE.")
    @GetMapping("/customers/orders")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'SALES_REPRESENTATIVE')")
    public ResponseEntity<List<CustomerOrderReport>> getCustomerOrderReport() {
        return new ResponseEntity<>(reportService.getCustomerOrderReport(), HttpStatus.OK);
    }

    @Operation(summary = "Payment summary report", description = "Returns total payments and payment methods breakdown. " +
            "BUSINESS_ANALYST, ADMINISTRATOR or FINANCIAL_MANAGER.")
    @GetMapping("/payments/summary")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'FINANCIAL_MANAGER')")
    public ResponseEntity<List<PaymentSummaryReport>> getPaymentSummary() {
        return new ResponseEntity<>(reportService.getPaymentSummary(), HttpStatus.OK);
    }
}