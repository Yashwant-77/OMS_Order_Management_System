package OMS_backend.controller;

import OMS_backend.dto.request.RecordPaymentRequest;
import OMS_backend.dto.response.PaymentResponse;
import OMS_backend.service.InvoiceService;
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
public class PaymentController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<PaymentResponse> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        return new ResponseEntity<>(invoiceService.recordPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_MANAGER', 'ADMINISTRATOR'," + "'BUSINESS_ANALYST', 'SALES_REPRESENTATIVE')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        return new ResponseEntity<>(invoiceService.getPaymentsByInvoice(invoiceId), HttpStatus.OK);
    }
}