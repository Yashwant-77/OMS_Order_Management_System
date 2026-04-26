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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceService Unit Tests")
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private SalesOrder confirmedOrder;
    private Invoice testInvoice;
    private CreateInvoiceRequest createInvoiceRequest;
    private RecordPaymentRequest recordPaymentRequest;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("ABC Corp");

        confirmedOrder = new SalesOrder();
        confirmedOrder.setSalesOrderId(1L);
        confirmedOrder.setCustomer(customer);
        confirmedOrder.setTotalAmount(30000.00);
        confirmedOrder.setStatus(OrderStatus.CONFIRMED);
        confirmedOrder.setOrderDate(LocalDateTime.now());
        confirmedOrder.setItems(new ArrayList<>());

        testInvoice = new Invoice();
        testInvoice.setInvoiceId(1L);
        testInvoice.setSalesOrder(confirmedOrder);
        testInvoice.setInvoiceDate(LocalDateTime.now());
        testInvoice.setTotalAmount(30000.00);
        testInvoice.setPaidAmount(0.00);
        testInvoice.setStatus(InvoiceStatus.PENDING);

        createInvoiceRequest = new CreateInvoiceRequest();
        createInvoiceRequest.setSalesOrderId(1L);

        recordPaymentRequest = new RecordPaymentRequest();
        recordPaymentRequest.setInvoiceId(1L);
        recordPaymentRequest.setAmount(10000.00);
        recordPaymentRequest.setPaymentMethod("BANK_TRANSFER");
        recordPaymentRequest.setNotes("First installment");
    }

    // ─── GENERATE INVOICE ─────────────────────────────────

    @Test
    @DisplayName("Generate Invoice - Success")
    void generateInvoice_Success() {
        when(salesOrderRepository.findById(1L))
                .thenReturn(Optional.of(confirmedOrder));
        when(invoiceRepository
                .existsBySalesOrder_SalesOrderId(1L))
                .thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(testInvoice);

        InvoiceResponse response =
                invoiceService.generateInvoice(createInvoiceRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualTo(30000.00);
        assertThat(response.getPaidAmount()).isEqualTo(0.00);
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Generate Invoice - Pending order throws BadRequestException")
    void generateInvoice_PendingOrder_ThrowsException() {
        confirmedOrder.setStatus(OrderStatus.PENDING);

        when(salesOrderRepository.findById(1L))
                .thenReturn(Optional.of(confirmedOrder));

        assertThatThrownBy(() ->
                invoiceService.generateInvoice(createInvoiceRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot generate invoice");
    }

    @Test
    @DisplayName("Generate Invoice - Duplicate throws DuplicateResourceException")
    void generateInvoice_Duplicate_ThrowsException() {
        when(salesOrderRepository.findById(1L))
                .thenReturn(Optional.of(confirmedOrder));
        when(invoiceRepository
                .existsBySalesOrder_SalesOrderId(1L))
                .thenReturn(true);

        assertThatThrownBy(() ->
                invoiceService.generateInvoice(createInvoiceRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Invoice already exists");
    }

    @Test
    @DisplayName("Generate Invoice - Order not found throws ResourceNotFoundException")
    void generateInvoice_OrderNotFound_ThrowsException() {
        when(salesOrderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                invoiceService.generateInvoice(createInvoiceRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── RECORD PAYMENT ───────────────────────────────────

    @Test
    @DisplayName("Record Payment - Partial payment updates status to PARTIALLY_PAID")
    void recordPayment_Partial_UpdatesStatusToPartiallyPaid() {
        Payment savedPayment = new Payment();
        savedPayment.setPaymentId(1L);
        savedPayment.setInvoice(testInvoice);
        savedPayment.setAmount(10000.00);
        savedPayment.setPaymentDate(LocalDateTime.now());
        savedPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(testInvoice));
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(testInvoice);

        PaymentResponse response =
                invoiceService.recordPayment(recordPaymentRequest);

        assertThat(testInvoice.getPaidAmount()).isEqualTo(10000.00);
        assertThat(testInvoice.getStatus())
                .isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    }

    @Test
    @DisplayName("Record Payment - Full payment updates status to PAID")
    void recordPayment_Full_UpdatesStatusToPaid() {
        recordPaymentRequest.setAmount(30000.00); // full amount

        Payment savedPayment = new Payment();
        savedPayment.setPaymentId(1L);
        savedPayment.setInvoice(testInvoice);
        savedPayment.setAmount(30000.00);
        savedPayment.setPaymentDate(LocalDateTime.now());
        savedPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(testInvoice));
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(testInvoice);

        invoiceService.recordPayment(recordPaymentRequest);

        assertThat(testInvoice.getPaidAmount()).isEqualTo(30000.00);
        assertThat(testInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    @DisplayName("Record Payment - Overpayment throws BadRequestException")
    void recordPayment_Overpayment_ThrowsException() {
        recordPaymentRequest.setAmount(99999.00); // more than total

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(testInvoice));

        assertThatThrownBy(() ->
                invoiceService.recordPayment(recordPaymentRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds outstanding amount");
    }

    @Test
    @DisplayName("Record Payment - Cancelled invoice throws BadRequestException")
    void recordPayment_CancelledInvoice_ThrowsException() {
        testInvoice.setStatus(InvoiceStatus.CANCELLED);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(testInvoice));

        assertThatThrownBy(() ->
                invoiceService.recordPayment(recordPaymentRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot record payment for a cancelled invoice");
    }
}