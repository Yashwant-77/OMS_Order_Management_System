package OMS_backend.controller;

import OMS_backend.dto.request.CreateInvoiceRequest;
import OMS_backend.dto.response.InvoiceResponse;
import OMS_backend.service.InvoiceService;
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
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Management", description = "Generate and track invoices for sales orders")
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "Generate invoice", description = "Generates invoice from a confirmed sales order. " +
            "FINANCIAL_MANAGER or ADMINISTRATOR.")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<InvoiceResponse> generateInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return new ResponseEntity<>(invoiceService.generateInvoice(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all invoices", description = "Returns all invoices. " +
            "FINANCIAL_MANAGER, ADMINISTRATOR, BUSINESS_ANALYST or SALES_REPRESENTATIVE.")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'SALES_REPRESENTATIVE')")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return new ResponseEntity<>(invoiceService.getAllInvoices(), HttpStatus.OK);
    }

    @Operation(summary = "Get invoice by ID", description = "Returns a single invoice by ID. " +
            "FINANCIAL_MANAGER, ADMINISTRATOR, BUSINESS_ANALYST or SALES_REPRESENTATIVE.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'SALES_REPRESENTATIVE')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return new ResponseEntity<>(invoiceService.getInvoiceById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update invoice status", description = "Updates the status of an invoice. " +
            "FINANCIAL_MANAGER or ADMINISTRATOR only.")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<InvoiceResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(invoiceService.updateInvoiceStatus(id, status), HttpStatus.OK);
    }
}