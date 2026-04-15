package OMS_backend.service;

import OMS_backend.dto.response.*;
import OMS_backend.model.InvoiceStatus;
import OMS_backend.model.OrderStatus;
import OMS_backend.model.SalesOrder;
import OMS_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SalesOrderRepository salesOrderRepository;

    private final InvoiceRepository invoiceRepository;

    private final PaymentRepository paymentRepository;

    private final ProductRepository productRepository;

    private final CustomerRepository customerRepository;

    // get summary of orders by status and total value
    public OrderSummaryReport getOrderSummary() {
        return new OrderSummaryReport(
                salesOrderRepository.count(),
                salesOrderRepository.countByStatus(OrderStatus.PENDING),
                salesOrderRepository.countByStatus(OrderStatus.CONFIRMED),
                salesOrderRepository.countByStatus(OrderStatus.PROCESSING),
                salesOrderRepository.countByStatus(OrderStatus.SHIPPED),
                salesOrderRepository.countByStatus(OrderStatus.DELIVERED),
                salesOrderRepository.countByStatus(OrderStatus.CANCELLED),
                salesOrderRepository.sumTotalOrderValue()
        );
    }

    //get revenue report with total invoiced, total collected, outstanding amount, and invoice counts by status
    public RevenueReport getRevenueReport() {
        double totalInvoiced = invoiceRepository.sumTotalInvoiced();
        double totalCollected = invoiceRepository.sumTotalCollected();

        return new RevenueReport(
                totalInvoiced,
                totalCollected,
                totalInvoiced - totalCollected,  // outstanding
                invoiceRepository.count(),
                invoiceRepository.countByStatus(InvoiceStatus.PAID),
                invoiceRepository.countByStatus(InvoiceStatus.PARTIALLY_PAID),
                invoiceRepository.countByStatus(InvoiceStatus.PENDING)
        );
    }

    //get low stock report for products at or below a specified threshold
    public List<LowStockReport> getLowStockReport(int threshold) {

        // return all products where stock is at or below threshold
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getQuantityInStock() <= threshold)
                .map(p -> new LowStockReport(
                        p.getProductId(),
                        p.getProductName(),
                        p.getQuantityInStock(),
                        p.getUnitPrice()
                ))
                .sorted(Comparator.comparingInt(LowStockReport::getQuantityInStock)) // lowest stock first
                .collect(Collectors.toList());
    }

    //get customer order report with total orders and total value, sorted by most orders
    public List<CustomerOrderReport> getCustomerOrderReport() {
        return customerRepository.findAll()
                .stream()
                .map(customer -> {
                    List<SalesOrder> orders = salesOrderRepository
                            .findByCustomer_CustomerId(customer.getCustomerId());

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
                        Long.compare(b.getTotalOrders(), a.getTotalOrders())) // most orders first
                .collect(Collectors.toList());
    }

    //get payment summary report with count and total amount by payment method
    public List<PaymentSummaryReport> getPaymentSummary() {
        return paymentRepository.findDistinctPaymentMethods()
                .stream()
                .map(method -> new PaymentSummaryReport(
                        method.name(),
                        paymentRepository.countByPaymentMethod(method),
                        paymentRepository.sumAmountByPaymentMethod(method)
                ))
                .collect(Collectors.toList());
    }
}