package OMS_backend.controller;

import OMS_backend.dto.request.BOMRequest;
import OMS_backend.dto.response.BOMResponse;
import OMS_backend.service.BOMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "BOM Management", description = "Bill of Materials — manage product components")
@SecurityRequirement(name = "Bearer Authentication")
public class BOMController {

    private final BOMService bomService;

    @Operation(summary = "Add component to BOM", description = "Adds a component to a product's BOM. "
            + "PRODUCT_MANAGER or ADMINISTRATOR only.")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<BOMResponse> addComponent(@Valid @RequestBody BOMRequest request) {
        return new ResponseEntity<>(bomService.addComponent(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Get BOM by product ID", description = "Returns the BOM for a specific product. " +
            "PRODUCT_MANAGER, ADMINISTRATOR, SALES_REPRESENTATIVE or BUSINESS_ANALYST only.")
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR'," + "'SALES_REPRESENTATIVE', 'BUSINESS_ANALYST')")
    public ResponseEntity<List<BOMResponse>> getBOM(@PathVariable Long productId) {
        return new ResponseEntity<>(bomService.getBOMByProductId(productId), HttpStatus.OK);
    }

    @Operation(summary = "Remove component from BOM", description = "Removes a component from a product's BOM. " +
            "PRODUCT_MANAGER or ADMINISTRATOR only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<String> removeComponent(@PathVariable Long id) {
        bomService.removeComponent(id);
        return new ResponseEntity<>("Component removed from BOM successfully", HttpStatus.OK);
    }
}