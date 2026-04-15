package OMS_backend.controller;

import OMS_backend.dto.request.CreateInvoiceRequest;
import OMS_backend.dto.response.InvoiceResponse;
import OMS_backend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<InvoiceResponse> generateInvoice(@RequestBody CreateInvoiceRequest request) {
        return new ResponseEntity<>(invoiceService.generateInvoice(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'SALES_REPRESENTATIVE')")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return new ResponseEntity<>(invoiceService.getAllInvoices(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'SALES_REPRESENTATIVE')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return new ResponseEntity<>(invoiceService.getInvoiceById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<InvoiceResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return new ResponseEntity<>(invoiceService.updateInvoiceStatus(id, status), HttpStatus.OK);
    }
}