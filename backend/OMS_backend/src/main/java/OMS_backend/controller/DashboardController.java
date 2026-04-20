package OMS_backend.controller;


import OMS_backend.dto.response.DashboardSummaryResponse;
import OMS_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'PRODUCT_MANAGER', 'FINANCE_MANAGER',"
            + "'BUSINESS_ANALYST', 'PURCHASING_OFFICER', 'SALES_REPRESENTATIVE')")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}
