package OMS_backend.controller;

import OMS_backend.dto.request.CustomerRequest;
import OMS_backend.dto.response.CustomerResponse;
import OMS_backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Create and view customers")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final OrderService orderService;

    @Operation(summary = "Create customer", description = "Creates a new customer. SALES_REPRESENTATIVE or ADMINISTRATOR.")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SALES_REPRESENTATIVE', 'ADMINISTRATOR')")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(orderService.createCustomer(request));
    }

    @Operation(summary = "Get all customers", description = "Returns list of all customers. "
            + "SALES_REPRESENTATIVE, ADMINISTRATOR or BUSINESS_ANALYST.")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SALES_REPRESENTATIVE', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(orderService.getAllCustomers());
    }
}