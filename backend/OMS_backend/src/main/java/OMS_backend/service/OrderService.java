package OMS_backend.service;

import OMS_backend.dto.request.CreateOrderRequest;
import OMS_backend.dto.request.CustomerRequest;
import OMS_backend.dto.request.OrderItemRequest;
import OMS_backend.dto.response.CustomerResponse;
import OMS_backend.dto.response.OrderItemResponse;
import OMS_backend.dto.response.OrderResponse;
import OMS_backend.exception.BadRequestException;
import OMS_backend.exception.DuplicateResourceException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.*;
import OMS_backend.repository.CustomerRepository;
import OMS_backend.repository.ProductRepository;
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
public class OrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    // customer operations
    public CustomerResponse createCustomer(CustomerRequest request) {

        log.info("Creating customer. email={}", request.getEmail());

        if (customerRepository.existsByEmail(request.getEmail())) {
            log.warn("Customer creation failed - duplicate email={}", request.getEmail());
            throw new DuplicateResourceException("Customer with this email already exists");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        Customer saved = customerRepository.save(customer);

        log.info("Customer created successfully. customerId={}, email={}",
                saved.getCustomerId(), saved.getEmail());

        return mapToCustomerResponse(saved);
    }

    public List<CustomerResponse> getAllCustomers() {

        log.info("Fetching all customers");

        List<CustomerResponse> customers = customerRepository.findAll()
                .stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} customers", customers.size());

        return customers;
    }

    // order operations
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        log.info("Creating order for customerId={}, itemsCount={}",
                request.getCustomerId(), request.getItems().size());

        // validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> {
                    log.error("Customer not found. customerId={}", request.getCustomerId());
                    return new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId());
                });

        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        double total = 0.0;

        for (OrderItemRequest itemRequest : request.getItems()) {

            log.debug("Processing order item. productId={}, quantity={}",
                    itemRequest.getProductId(), itemRequest.getQuantity());

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> {
                        log.error("Product not found. productId={}", itemRequest.getProductId());
                        return new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId());
                    });

            // stock check
            if (product.getQuantityInStock() < itemRequest.getQuantity()) {
                log.warn("Insufficient stock. productId={}, requested={}, available={}",
                        product.getProductId(), itemRequest.getQuantity(), product.getQuantityInStock());

                throw new BadRequestException("Insufficient stock for: " + product.getProductName()
                        + ". Available: " + product.getQuantityInStock());
            }

            // deduct stock
            int oldStock = product.getQuantityInStock();
            product.setQuantityInStock(oldStock - itemRequest.getQuantity());
            productRepository.save(product);

            log.debug("Stock updated. productId={}, oldStock={}, newStock={}",
                    product.getProductId(), oldStock, product.getQuantityInStock());

            // build item
            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getUnitPrice());
            order.getItems().add(item);

            total += product.getUnitPrice() * itemRequest.getQuantity();
        }

        order.setTotalAmount(total);

        SalesOrder savedOrder = salesOrderRepository.save(order);

        log.info("Order created successfully. orderId={}, totalAmount={}",
                savedOrder.getSalesOrderId(), total);

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        log.info("Fetching all orders");

        List<OrderResponse> orders = salesOrderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} orders", orders.size());

        System.out.println(orders.size());

        return orders;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {

        log.info("Fetching order by id={}", id);

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found. id={}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });

        return mapToOrderResponse(order);
    }

    public OrderResponse updateOrderStatus(Long id, String status) {

        log.info("Updating order status. orderId={}, newStatus={}", id, status);

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found for status update. id={}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });

        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            log.error("Invalid order status: {}", status);
            throw new BadRequestException("Invalid order status: " + status);
        }

        SalesOrder updated = salesOrderRepository.save(order);

        log.info("Order status updated. orderId={}, status={}", id, updated.getStatus());

        return mapToOrderResponse(updated);
    }

    @Transactional
    public void cancelOrder(Long id) {

        log.info("Cancelling order. orderId={}", id);

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found for cancellation. id={}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });

        if (order.getStatus() == OrderStatus.DELIVERED) {
            log.warn("Attempt to cancel delivered order. orderId={}", id);
            throw new BadRequestException("Cannot cancel a delivered order");
        }

        // restore stock
        for (SalesOrderItem item : order.getItems()) {

            Product product = item.getProduct();
            int oldStock = product.getQuantityInStock();

            product.setQuantityInStock(oldStock + item.getQuantity());
            productRepository.save(product);

            log.debug("Stock restored. productId={}, restoredQty={}, newStock={}",
                    product.getProductId(), item.getQuantity(), product.getQuantityInStock());
        }

        order.setStatus(OrderStatus.CANCELLED);
        salesOrderRepository.save(order);

        log.info("Order cancelled successfully. orderId={}", id);
    }

    private OrderResponse mapToOrderResponse(SalesOrder order) {

        log.debug("Mapping Order to OrderResponse. orderId={}", order.getSalesOrderId());

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getSalesOrderItemId(),
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice() * item.getQuantity()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getSalesOrderId(),
                order.getCustomer().getCustomerId(),
                order.getCustomer().getName(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus().name(),
                items
        );
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {

        log.debug("Mapping Customer to CustomerResponse. customerId={}", customer.getCustomerId());

        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
        );
    }
}