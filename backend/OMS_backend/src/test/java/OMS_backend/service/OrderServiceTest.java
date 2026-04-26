package OMS_backend.service;

import OMS_backend.dto.request.CreateOrderRequest;
import OMS_backend.dto.request.OrderItemRequest;
import OMS_backend.dto.response.OrderResponse;
import OMS_backend.exception.BadRequestException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.*;
import OMS_backend.repository.CustomerRepository;
import OMS_backend.repository.ProductRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Product testProduct;
    private SalesOrder testOrder;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setCustomerId(1L);
        testCustomer.setName("ABC Corp");
        testCustomer.setEmail("abc@corp.com");

        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setProductName("UPS System");
        testProduct.setUnitPrice(15000.00);
        testProduct.setQuantityInStock(50);

        testOrder = new SalesOrder();
        testOrder.setSalesOrderId(1L);
        testOrder.setCustomer(testCustomer);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalAmount(30000.00);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setItems(new ArrayList<>());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerId(1L);
        createOrderRequest.setItems(List.of(itemRequest));
    }

    // ─── CREATE ORDER ─────────────────────────────────────

    @Test
    @DisplayName("Create Order - Success")
    void createOrder_Success() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(salesOrderRepository.save(any(SalesOrder.class)))
                .thenReturn(testOrder);

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo("ABC Corp");
        assertThat(response.getStatus()).isEqualTo("PENDING");

        // Stock should be deducted
        verify(productRepository, atLeastOnce()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Order - Customer not found throws ResourceNotFoundException")
    void createOrder_CustomerNotFound_ThrowsException() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Order - Product not found throws ResourceNotFoundException")
    void createOrder_ProductNotFound_ThrowsException() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Create Order - Insufficient stock throws BadRequestException")
    void createOrder_InsufficientStock_ThrowsException() {
        testProduct.setQuantityInStock(1); // only 1 in stock, ordering 2

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    // ─── CANCEL ORDER ─────────────────────────────────────

    @Test
    @DisplayName("Cancel Order - Success restores stock")
    void cancelOrder_Success_RestoresStock() {
        SalesOrderItem item = new SalesOrderItem();
        item.setProduct(testProduct);
        item.setQuantity(2);
        testOrder.setItems(List.of(item));
        testOrder.setStatus(OrderStatus.CONFIRMED);

        when(salesOrderRepository.findById(1L))
                .thenReturn(Optional.of(testOrder));

        orderService.cancelOrder(1L);

        // Stock should be restored
        assertThat(testProduct.getQuantityInStock()).isEqualTo(52);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(salesOrderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Cancel Order - Delivered order throws BadRequestException")
    void cancelOrder_DeliveredOrder_ThrowsException() {
        testOrder.setStatus(OrderStatus.DELIVERED);

        when(salesOrderRepository.findById(1L))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot cancel a delivered order");
    }

    @Test
    @DisplayName("Cancel Order - Not found throws ResourceNotFoundException")
    void cancelOrder_NotFound_ThrowsException() {
        when(salesOrderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── UPDATE STATUS ────────────────────────────────────

    @Test
    @DisplayName("Update Order Status - Success")
    void updateOrderStatus_Success() {
        when(salesOrderRepository.findById(1L))
                .thenReturn(Optional.of(testOrder));
        when(salesOrderRepository.save(any(SalesOrder.class)))
                .thenReturn(testOrder);

        OrderResponse response = orderService.updateOrderStatus(1L, "CONFIRMED");

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
}