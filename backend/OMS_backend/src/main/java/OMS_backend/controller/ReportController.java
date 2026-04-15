package OMS_backend.controller;

import OMS_backend.dto.response.*;
import OMS_backend.service.ReportService;
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
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/orders/summary")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'SALES_REPRESENTATIVE')")
    public ResponseEntity<OrderSummaryReport> getOrderSummary() {
        return new ResponseEntity<>(reportService.getOrderSummary(), HttpStatus.OK);
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'FINANCIAL_MANAGER')")
    public ResponseEntity<RevenueReport> getRevenueReport() {
        return new ResponseEntity<>(reportService.getRevenueReport(), HttpStatus.OK);
    }

    @GetMapping("/products/low-stock")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'PRODUCT_MANAGER', 'PURCHASING_OFFICER')")
    public ResponseEntity<List<LowStockReport>> getLowStockReport(@RequestParam(defaultValue = "10") int threshold) {
        return new ResponseEntity<>(reportService.getLowStockReport(threshold), HttpStatus.OK);
    }

    @GetMapping("/customers/orders")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'SALES_REPRESENTATIVE')")
    public ResponseEntity<List<CustomerOrderReport>> getCustomerOrderReport() {
        return new ResponseEntity<>(reportService.getCustomerOrderReport(), HttpStatus.OK);
    }

    @GetMapping("/payments/summary")
    @PreAuthorize("hasAnyAuthority('BUSINESS_ANALYST', 'ADMINISTRATOR'," + "'FINANCIAL_MANAGER')")
    public ResponseEntity<List<PaymentSummaryReport>> getPaymentSummary() {
        return new ResponseEntity<>(reportService.getPaymentSummary(), HttpStatus.OK);
    }
}