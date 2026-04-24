package OMS_backend.controller;

import OMS_backend.dto.request.RecordPaymentRequest;
import OMS_backend.dto.response.PaymentResponse;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management",
        description = "Record and track payments against invoices")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final InvoiceService invoiceService;

    @Operation(summary = "Record payment", description = "Records a payment against an invoice. " +
            "Auto-updates invoice status when fully paid." + " FINANCIAL_MANAGER or ADMINISTRATOR only.")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<PaymentResponse> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        return new ResponseEntity<>(invoiceService.recordPayment(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Get payments by invoice", description = "Returns all payments made against a specific invoice. " +
            "FINANCIAL_MANAGER, ADMINISTRATOR, BUSINESS_ANALYST or SALES_REPRESENTATIVE.")
    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'SALES_REPRESENTATIVE')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        return new ResponseEntity<>(invoiceService.getPaymentsByInvoice(invoiceId), HttpStatus.OK);
    }
}