package OMS_backend.controller;

import OMS_backend.dto.request.CreateOrderRequest;
import OMS_backend.dto.response.OrderResponse;
import OMS_backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Sales order creation, tracking and management")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create sales order", description = "Creates a new sales order with items. Automatically deducts stock."
            + "SALES_REPRESENTATIVE or ADMINISTRATOR only.")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SALES_REPRESENTATIVE', 'ADMINISTRATOR')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return new ResponseEntity<>(orderService.createOrder(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all orders", description = "Returns all sales orders." +
            " SALES_REPRESENTATIVE, ADMINISTRATOR, BUSINESS_ANALYST or PRODUCT_MANAGER.")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SALES_REPRESENTATIVE', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'PRODUCT_MANAGER')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

    @Operation(summary = "Get all orders", description = "Returns all sales orders." +
            " SALES_REPRESENTATIVE, ADMINISTRATOR, BUSINESS_ANALYST or PRODUCT_MANAGER.")
    @GetMapping("/filteredOrders")
    @PreAuthorize("hasAnyAuthority('SALES_REPRESENTATIVE', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'PRODUCT_MANAGER')")
    public Page<OrderResponse> getFilteredOrders(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        return orderService.getFilteredOrders(page, size, status, search);
    }

    @Operation(summary = "Get order by ID", description = "Returns a single sales order by ID. " +
            "SALES_REPRESENTATIVE, ADMINISTRATOR, BUSINESS_ANALYST or PRODUCT_MANAGER.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SALES_REPRESENTATIVE', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'PRODUCT_MANAGER')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return new ResponseEntity<>(orderService.getOrderById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update order status", description = "Updates the status of a sales order. " +
            "ADMINISTRATOR or PRODUCT_MANAGER only.")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'PRODUCT_MANAGER')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(orderService.updateOrderStatus(id, status), HttpStatus.OK);
    }

    @Operation(summary = "Cancel order", description = "Cancels a sales order and restores stock. " +
            "SALES_REPRESENTATIVE or ADMINISTRATOR only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SALES_REPRESENTATIVE', 'ADMINISTRATOR')")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return new ResponseEntity<>("Order cancelled successfully", HttpStatus.OK);
    }
}