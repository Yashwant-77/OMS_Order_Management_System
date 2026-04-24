package OMS_backend.controller;

import OMS_backend.model.Product;
import OMS_backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management",
        description = "Create and view products")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create product", description = "Creates a new product. PRODUCT_MANAGER or ADMINISTRATOR only.")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return new ResponseEntity<>(productService.createProduct(product), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all products", description = "Returns list of all products. " +
            "PRODUCT_MANAGER, ADMINISTRATOR, SALES_REPRESENTATIVE, PURCHASING_OFFICER or BUSINESS_ANALYST.")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR'," + "'SALES_REPRESENTATIVE', 'PURCHASING_OFFICER', 'BUSINESS_ANALYST')")
    public ResponseEntity<List<Product>> getAllProducts() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @Operation(summary = "Get product by ID", description = "Returns a single product by ID. " +
            "PRODUCT_MANAGER, ADMINISTRATOR, SALES_REPRESENTATIVE or PURCHASING_OFFICER.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMINISTRATOR'," + "'SALES_REPRESENTATIVE', 'PURCHASING_OFFICER')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return new ResponseEntity<>(productService.getProductById(id), HttpStatus.OK);
    }
}