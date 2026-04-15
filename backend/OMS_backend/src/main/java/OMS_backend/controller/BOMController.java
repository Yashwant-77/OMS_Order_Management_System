package OMS_backend.controller;

import OMS_backend.dto.request.BOMRequest;
import OMS_backend.dto.response.BOMResponse;
import OMS_backend.service.BOMService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bom")
@RequiredArgsConstructor
public class BOMController {

    private final BOMService bomService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<BOMResponse> addComponent(@Valid @RequestBody BOMRequest request) {
        return new ResponseEntity<>(bomService.addComponent(request), HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR'," + "'SALES_REPRESENTATIVE', 'BUSINESS_ANALYST')")
    public ResponseEntity<List<BOMResponse>> getBOM(@PathVariable Long productId) {
        return new ResponseEntity<>(bomService.getBOMByProductId(productId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<String> removeComponent(@PathVariable Long id) {
        bomService.removeComponent(id);
        return new ResponseEntity<>("Component removed from BOM successfully", HttpStatus.OK);
    }
}