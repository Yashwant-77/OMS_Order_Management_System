package OMS_backend.service;

import OMS_backend.dto.request.CreateInvoiceRequest;
import OMS_backend.dto.request.RecordPaymentRequest;
import OMS_backend.dto.response.InvoiceResponse;
import OMS_backend.dto.response.PaymentResponse;
import OMS_backend.exception.BadRequestException;
import OMS_backend.exception.DuplicateResourceException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.*;
import OMS_backend.repository.InvoiceRepository;
import OMS_backend.repository.PaymentRepository;
import OMS_backend.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SalesOrderRepository salesOrderRepository;

    // generate invoice for a sales order
    @Transactional
    public InvoiceResponse generateInvoice(CreateInvoiceRequest request) {

        log.info("Generating invoice for salesOrderId={}", request.getSalesOrderId());

        // validate sales order exists
        SalesOrder salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
                .orElseThrow(() -> {
                    log.error("Sales order not found. salesOrderId={}", request.getSalesOrderId());
                    return new ResourceNotFoundException("Sales order not found with id: " + request.getSalesOrderId());
                });

        // only confirmed/processing orders can be invoiced
        if (salesOrder.getStatus() == OrderStatus.PENDING || salesOrder.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Invalid invoice attempt for salesOrderId={} with status={}",
                    salesOrder.getSalesOrderId(), salesOrder.getStatus());
            throw new BadRequestException("Cannot generate invoice for order with status: " + salesOrder.getStatus());
        }

        // prevent duplicate invoice
        if (invoiceRepository.existsBySalesOrder_SalesOrderId(request.getSalesOrderId())) {
            log.warn("Duplicate invoice attempt for salesOrderId={}", request.getSalesOrderId());
            throw new DuplicateResourceException("Invoice already exists for order id: " + request.getSalesOrderId());
        }

        // create invoice
        Invoice invoice = new Invoice();
        invoice.setSalesOrder(salesOrder);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setTotalAmount(salesOrder.getTotalAmount());
        invoice.setPaidAmount(0.0);
        invoice.setStatus(InvoiceStatus.PENDING);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info("Invoice created successfully. invoiceId={}, salesOrderId={}",
                savedInvoice.getInvoiceId(), request.getSalesOrderId());

        return mapToInvoiceResponse(savedInvoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {

        log.info("Fetching all invoices");

        List<InvoiceResponse> response = invoiceRepository.findAll()
                .stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} invoices", response.size());

        return response;
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {

        log.info("Fetching invoice by id={}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Invoice not found. id={}", id);
                    return new ResourceNotFoundException("Invoice not found with id: " + id);
                });

        return mapToInvoiceResponse(invoice);
    }

    @Transactional
    public InvoiceResponse updateInvoiceStatus(Long id, String status) {

        log.info("Updating invoice status. id={}, newStatus={}", id, status);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Invoice not found for status update. id={}", id);
                    return new ResourceNotFoundException("Invoice not found with id: " + id);
                });

        try {
            invoice.setStatus(InvoiceStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            log.error("Invalid invoice status provided: {}", status);
            throw new BadRequestException("Invalid invoice status: " + status);
        }

        Invoice updated = invoiceRepository.save(invoice);

        log.info("Invoice status updated successfully. id={}, status={}", id, updated.getStatus());

        return mapToInvoiceResponse(updated);
    }

    // record a payment against an invoice
    @Transactional
    public PaymentResponse recordPayment(RecordPaymentRequest request) {

        log.info("Recording payment. invoiceId={}, amount={}", request.getInvoiceId(), request.getAmount());

        // validate invoice
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> {
                    log.error("Invoice not found for payment. invoiceId={}", request.getInvoiceId());
                    return new ResourceNotFoundException("Invoice not found with id: " + request.getInvoiceId());
                });

        // cannot pay a cancelled invoice
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            log.warn("Payment attempt on cancelled invoice. invoiceId={}", request.getInvoiceId());
            throw new BadRequestException("Cannot record payment for a cancelled invoice");
        }

        // cannot overpay
        double outstanding = invoice.getTotalAmount() - invoice.getPaidAmount();
        if (request.getAmount() > outstanding) {
            log.warn("Overpayment attempt. invoiceId={}, amount={}, outstanding={}",
                    request.getInvoiceId(), request.getAmount(), outstanding);
            throw new BadRequestException("Payment amount exceeds outstanding amount of ₹" + outstanding);
        }

        // record payment
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(request.getAmount());

        try {
            payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            log.error("Invalid payment method: {}", request.getPaymentMethod());
            throw new BadRequestException("Invalid payment method: " + request.getPaymentMethod());
        }

        payment.setNotes(request.getNotes());
        Payment savedPayment = paymentRepository.save(payment);

        // update paid amount
        double newPaidAmount = invoice.getPaidAmount() + request.getAmount();
        invoice.setPaidAmount(newPaidAmount);

        // update status
        if (newPaidAmount >= invoice.getTotalAmount()) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        log.info("Payment recorded successfully. paymentId={}, invoiceId={}, newStatus={}",
                savedPayment.getPaymentId(), invoice.getInvoiceId(), invoice.getStatus());

        return mapToPaymentResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {

        log.info("Fetching payments for invoiceId={}", invoiceId);

        if (!invoiceRepository.existsById(invoiceId)) {
            log.error("Invoice not found while fetching payments. invoiceId={}", invoiceId);
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }

        List<PaymentResponse> response = paymentRepository.findByInvoice_InvoiceId(invoiceId)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} payments for invoiceId={}", response.size(), invoiceId);

        return response;
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        log.debug("Mapping Invoice to InvoiceResponse. invoiceId={}", invoice.getInvoiceId());

        return new InvoiceResponse(
                invoice.getInvoiceId(),
                invoice.getSalesOrder().getSalesOrderId(),
                invoice.getSalesOrder().getCustomer().getName(),
                invoice.getInvoiceDate(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getTotalAmount() - invoice.getPaidAmount(),
                invoice.getStatus().name()
        );
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        log.debug("Mapping Payment to PaymentResponse. paymentId={}", payment.getPaymentId());

        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getInvoice().getInvoiceId(),
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getNotes()
        );
    }
}