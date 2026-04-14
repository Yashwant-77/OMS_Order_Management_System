package OMS_backend.controller;

import OMS_backend.dto.request.SupplierRequest;
import OMS_backend.dto.response.SupplierResponse;
import OMS_backend.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PURCHASING_OFFICER', 'ADMINISTRATOR')")
    public ResponseEntity<SupplierResponse> createSupplier(@RequestBody SupplierRequest request) {
        return new ResponseEntity<>(purchaseOrderService.createSupplier(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PURCHASING_OFFICER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST')")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return new ResponseEntity<>(purchaseOrderService.getAllSuppliers(), HttpStatus.OK);
    }
}