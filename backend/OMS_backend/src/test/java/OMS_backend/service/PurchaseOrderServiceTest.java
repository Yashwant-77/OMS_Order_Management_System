package OMS_backend.service;

import OMS_backend.dto.request.CreatePurchaseOrderRequest;
import OMS_backend.dto.request.PurchaseOrderItemRequest;
import OMS_backend.dto.request.SupplierRequest;
import OMS_backend.dto.response.PurchaseOrderResponse;
import OMS_backend.dto.response.SupplierResponse;
import OMS_backend.exception.BadRequestException;
import OMS_backend.exception.DuplicateResourceException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.*;
import OMS_backend.repository.ProductRepository;
import OMS_backend.repository.PurchaseOrderRepository;
import OMS_backend.repository.SupplierRepository;
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
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderService Unit Tests")
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private Supplier testSupplier;
    private Product testProduct;
    private PurchaseOrder testPO;
    private SupplierRequest supplierRequest;
    private CreatePurchaseOrderRequest createPORequest;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setSupplierId(1L);
        testSupplier.setName("PowerTech Supplies");
        testSupplier.setEmail("supply@powertech.com");

        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setProductName("Battery Cell");
        testProduct.setUnitPrice(2000.00);
        testProduct.setQuantityInStock(100);

        testPO = new PurchaseOrder();
        testPO.setPurchaseOrderId(1L);
        testPO.setSupplier(testSupplier);
        testPO.setOrderDate(LocalDateTime.now());
        testPO.setTotalAmount(360000.00);
        testPO.setStatus(PurchaseOrderStatus.PENDING);
        testPO.setItems(new ArrayList<>());

        supplierRequest = new SupplierRequest();
        supplierRequest.setName("PowerTech Supplies");
        supplierRequest.setEmail("supply@powertech.com");
        supplierRequest.setPhone("8888888888");
        supplierRequest.setAddress("Mumbai, MH");

        PurchaseOrderItemRequest itemRequest =
                new PurchaseOrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(200);
        itemRequest.setUnitPrice(1800.00);

        createPORequest = new CreatePurchaseOrderRequest();
        createPORequest.setSupplierId(1L);
        createPORequest.setItems(List.of(itemRequest));
    }

    @Test
    @DisplayName("Create Supplier - Success")
    void createSupplier_Success() {
        when(supplierRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(supplierRepository.save(any(Supplier.class)))
                .thenReturn(testSupplier);

        SupplierResponse response =
                purchaseOrderService.createSupplier(supplierRequest);

        assertThat(response.getName()).isEqualTo("PowerTech Supplies");
        assertThat(response.getEmail()).isEqualTo("supply@powertech.com");
    }

    @Test
    @DisplayName("Create Supplier - Duplicate email throws DuplicateResourceException")
    void createSupplier_DuplicateEmail_ThrowsException() {
        when(supplierRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                purchaseOrderService.createSupplier(supplierRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Create Purchase Order - Success")
    void createPurchaseOrder_Success() {
        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(testSupplier));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenReturn(testPO);

        PurchaseOrderResponse response =
                purchaseOrderService.createPurchaseOrder(createPORequest);

        assertThat(response).isNotNull();
        assertThat(response.getSupplierName())
                .isEqualTo("PowerTech Supplies");
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Create Purchase Order - Supplier not found throws ResourceNotFoundException")
    void createPurchaseOrder_SupplierNotFound_ThrowsException() {
        when(supplierRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                purchaseOrderService.createPurchaseOrder(createPORequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found");
    }

    @Test
    @DisplayName("Update Status to RECEIVED - Auto updates stock")
    void updateStatus_Received_UpdatesStock() {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProduct(testProduct);
        item.setQuantity(200);
        item.setUnitPrice(1800.00);
        testPO.setItems(List.of(item));

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(testPO));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenReturn(testPO);

        purchaseOrderService.updateStatus(1L, "RECEIVED");

        assertThat(testProduct.getQuantityInStock()).isEqualTo(300);
        assertThat(testPO.getStatus())
                .isEqualTo(PurchaseOrderStatus.RECEIVED);
    }

    @Test
    @DisplayName("Cancel PO - Already received throws BadRequestException")
    void cancelPO_AlreadyReceived_ThrowsException() {
        testPO.setStatus(PurchaseOrderStatus.RECEIVED);

        when(purchaseOrderRepository.findById(1L))
                .thenReturn(Optional.of(testPO));

        assertThatThrownBy(() ->
                purchaseOrderService.cancelPurchaseOrder(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already received");
    }
}