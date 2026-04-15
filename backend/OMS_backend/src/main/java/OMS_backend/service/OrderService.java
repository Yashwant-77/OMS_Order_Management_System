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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final SalesOrderRepository salesOrderRepository;

    private final CustomerRepository customerRepository;

    private final ProductRepository productRepository;

    // customer operations
    public CustomerResponse createCustomer(CustomerRequest request) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer with this email already exists");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        return mapToCustomerResponse(customerRepository.save(customer));
    }

    // read operations
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());
    }

    // order operations
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        // validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        // build order
        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // build order items & calculate total
        double total = 0.0;

        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));

            // check stock availability
            if (product.getQuantityInStock() < itemRequest.getQuantity()) {
                throw new BadRequestException("Insufficient stock for: " + product.getProductName()
                        + ". Available: " + product.getQuantityInStock());
            }

            // deduct stock
            product.setQuantityInStock(product.getQuantityInStock() - itemRequest.getQuantity());
            productRepository.save(product);

            // build item
            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getUnitPrice()); // snapshot price
            order.getItems().add(item);

            total += product.getUnitPrice() * itemRequest.getQuantity();
        }

        order.setTotalAmount(total);

        return mapToOrderResponse(salesOrderRepository.save(order));
    }

    // read operations
    public List<OrderResponse> getAllOrders() {
        return salesOrderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    // read single order with details
    public OrderResponse getOrderById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    // update order status (e.g. PENDING → SHIPPED → DELIVERED)
    public OrderResponse updateOrderStatus(Long id, String status) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        return mapToOrderResponse(salesOrderRepository.save(order));
    }

    // cancel order (only if not delivered, and restore stock)
    @Transactional
    public void cancelOrder(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order");
        }

        // restore stock for each item
        for (SalesOrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        salesOrderRepository.save(order);
    }

    // helper methods to map entities to response DTOs
    private OrderResponse mapToOrderResponse(SalesOrder order) {
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

    // helper method to map Customer entity to CustomerResponse DTO
    private CustomerResponse mapToCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
        );
    }
}