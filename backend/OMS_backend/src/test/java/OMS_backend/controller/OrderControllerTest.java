package OMS_backend.controller;

import OMS_backend.config.SecurityConfig;
import OMS_backend.dto.request.CreateOrderRequest;
import OMS_backend.dto.request.OrderItemRequest;
import OMS_backend.dto.response.OrderResponse;
import OMS_backend.exception.BadRequestException;
import OMS_backend.exception.GlobalExceptionHandler;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.security.JwtUtil;
import OMS_backend.service.CustomUserDetailsService;
import OMS_backend.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("OrderController Integration Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private OrderResponse testOrderResponse;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        testOrderResponse = new OrderResponse(
                1L, 1L, "ABC Corp",
                LocalDateTime.now(),
                30000.00, "PENDING",
                new ArrayList<>()
        );

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerId(1L);
        createOrderRequest.setItems(List.of(item));
    }

    @Test
    @DisplayName("POST /api/orders - Success with SALES_REPRESENTATIVE role")
    @WithMockUser(authorities = "SALES_REPRESENTATIVE")
    void createOrder_Success_Returns201() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(testOrderResponse);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerName").value("ABC Corp"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/orders - Unauthenticated returns 401")
    void createOrder_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createOrderRequest)))
                .andExpect(status().isUnauthorized());  // 401 with our SecurityConfig
    }

    @Test
    @DisplayName("POST /api/orders - Wrong role returns 403")
    @WithMockUser(authorities = "FINANCIAL_MANAGER")
    void createOrder_WrongRole_Returns403() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createOrderRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/orders - Returns list with valid role")
    @WithMockUser(authorities = "SALES_REPRESENTATIVE")
    void getAllOrders_Returns200() throws Exception {
        when(orderService.getAllOrders())
                .thenReturn(List.of(testOrderResponse));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Not found returns 404")
    @WithMockUser(authorities = "ADMINISTRATOR")
    void getOrderById_NotFound_Returns404() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "Order not found with id: 99"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Order not found with id: 99"));
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Success with SALES_REPRESENTATIVE")
    @WithMockUser(authorities = "SALES_REPRESENTATIVE")
    void cancelOrder_Success_Returns200() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/orders - Insufficient stock returns 400")
    @WithMockUser(authorities = "SALES_REPRESENTATIVE")
    void createOrder_InsufficientStock_Returns400() throws Exception {
        when(orderService.createOrder(any()))
                .thenThrow(new BadRequestException(
                        "Insufficient stock for: UPS System. Available: 1"));

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Insufficient stock for: UPS System. Available: 1"));
    }
}