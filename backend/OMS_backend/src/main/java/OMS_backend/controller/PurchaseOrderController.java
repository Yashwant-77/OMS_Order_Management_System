package OMS_backend.controller;

import OMS_backend.dto.request.CreatePurchaseOrderRequest;
import OMS_backend.dto.response.PurchaseOrderResponse;
import OMS_backend.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PURCHASING_OFFICER', 'ADMINISTRATOR')")
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@RequestBody CreatePurchaseOrderRequest request) {
        return new ResponseEntity<>(purchaseOrderService.createPurchaseOrder(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PURCHASING_OFFICER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'PRODUCT_MANAGER')")
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        return new ResponseEntity<>(purchaseOrderService.getAllPurchaseOrders(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PURCHASING_OFFICER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'PRODUCT_MANAGER')")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long id) {
        return new ResponseEntity<>(purchaseOrderService.getPurchaseOrderById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('PURCHASING_OFFICER', 'ADMINISTRATOR')")
    public ResponseEntity<PurchaseOrderResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(purchaseOrderService.updateStatus(id, status), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PURCHASING_OFFICER', 'ADMINISTRATOR')")
    public ResponseEntity<String> cancelPurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.cancelPurchaseOrder(id);
        return new ResponseEntity<>("Purchase order cancelled successfully", HttpStatus.OK);
    }
}