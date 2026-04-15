package OMS_backend.service;

import OMS_backend.dto.response.*;
import OMS_backend.model.InvoiceStatus;
import OMS_backend.model.OrderStatus;
import OMS_backend.model.SalesOrder;
import OMS_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final SalesOrderRepository salesOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    // order summary report
    public OrderSummaryReport getOrderSummary() {

        log.info("Generating Order Summary Report");

        long totalOrders = salesOrderRepository.count();
        long pending = salesOrderRepository.countByStatus(OrderStatus.PENDING);
        long confirmed = salesOrderRepository.countByStatus(OrderStatus.CONFIRMED);
        long processing = salesOrderRepository.countByStatus(OrderStatus.PROCESSING);
        long shipped = salesOrderRepository.countByStatus(OrderStatus.SHIPPED);
        long delivered = salesOrderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelled = salesOrderRepository.countByStatus(OrderStatus.CANCELLED);
        double totalValue = salesOrderRepository.sumTotalOrderValue();

        log.info("Order Summary: total={}, delivered={}, cancelled={}, totalValue={}",
                totalOrders, delivered, cancelled, totalValue);

        return new OrderSummaryReport(
                totalOrders,
                pending,
                confirmed,
                processing,
                shipped,
                delivered,
                cancelled,
                totalValue
        );
    }

    // revenue report
    public RevenueReport getRevenueReport() {

        log.info("Generating Revenue Report");

        double totalInvoiced = invoiceRepository.sumTotalInvoiced();
        double totalCollected = invoiceRepository.sumTotalCollected();
        double outstanding = totalInvoiced - totalCollected;

        log.info("Revenue Summary: invoiced={}, collected={}, outstanding={}",
                totalInvoiced, totalCollected, outstanding);

        return new RevenueReport(
                totalInvoiced,
                totalCollected,
                outstanding,
                invoiceRepository.count(),
                invoiceRepository.countByStatus(InvoiceStatus.PAID),
                invoiceRepository.countByStatus(InvoiceStatus.PARTIALLY_PAID),
                invoiceRepository.countByStatus(InvoiceStatus.PENDING)
        );
    }

    // low stock report
    public List<LowStockReport> getLowStockReport(int threshold) {

        log.info("Generating Low Stock Report. threshold={}", threshold);

        List<LowStockReport> list = productRepository.findAll()
                .stream()
                .filter(p -> p.getQuantityInStock() <= threshold)
                .map(p -> new LowStockReport(
                        p.getProductId(),
                        p.getProductName(),
                        p.getQuantityInStock(),
                        p.getUnitPrice()
                ))
                .sorted(Comparator.comparingInt(LowStockReport::getQuantityInStock))
                .collect(Collectors.toList());

        log.info("Low stock items found: {}", list.size());

        return list;
    }

    // customer order report
    public List<CustomerOrderReport> getCustomerOrderReport() {

        log.info("Generating Customer Order Report");

        List<CustomerOrderReport> list = customerRepository.findAll()
                .stream()
                .map(customer -> {

                    List<SalesOrder> orders =
                            salesOrderRepository.findByCustomer_CustomerId(customer.getCustomerId());

                    double totalValue = orders.stream()
                            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                            .mapToDouble(SalesOrder::getTotalAmount)
                            .sum();

                    return new CustomerOrderReport(
                            customer.getCustomerId(),
                            customer.getName(),
                            customer.getEmail(),
                            orders.size(),
                            totalValue
                    );
                })
                .sorted((a, b) ->
                        Long.compare(b.getTotalOrders(), a.getTotalOrders()))
                .collect(Collectors.toList());

        log.info("Customer report generated for {} customers", list.size());

        return list;
    }

    // payment summary report
    public List<PaymentSummaryReport> getPaymentSummary() {

        log.info("Generating Payment Summary Report");

        List<PaymentSummaryReport> list = paymentRepository.findDistinctPaymentMethods()
                .stream()
                .map(method -> new PaymentSummaryReport(
                        method.name(),
                        paymentRepository.countByPaymentMethod(method),
                        paymentRepository.sumAmountByPaymentMethod(method)
                ))
                .collect(Collectors.toList());

        log.info("Payment summary generated for {} payment methods", list.size());

        return list;
    }
}