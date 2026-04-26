package OMS_backend.service;

import OMS_backend.dto.request.BOMRequest;
import OMS_backend.dto.response.BOMResponse;
import OMS_backend.exception.BadRequestException;
import OMS_backend.exception.DuplicateResourceException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.Product;
import OMS_backend.model.ProductBOM;
import OMS_backend.repository.ProductBOMRepository;
import OMS_backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BOMService Unit Tests")
class BOMServiceTest {

    @Mock
    private ProductBOMRepository productBOMRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BOMService bomService;

    private Product product;
    private Product component;
    private BOMRequest bomRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(1L);
        product.setProductName("UPS System");
        product.setUnitPrice(15000.00);
        product.setQuantityInStock(50);

        component = new Product();
        component.setProductId(2L);
        component.setProductName("Battery Cell");
        component.setUnitPrice(2000.00);
        component.setQuantityInStock(500);

        bomRequest = new BOMRequest();
        bomRequest.setProductId(1L);
        bomRequest.setComponentId(2L);
        bomRequest.setQuantity(4);
    }

    @Test
    @DisplayName("Add Component - Success")
    void addComponent_Success() {
        ProductBOM savedBom = new ProductBOM();
        savedBom.setProductBomId(1L);
        savedBom.setProduct(product);
        savedBom.setComponent(component);
        savedBom.setQuantity(4);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.findById(2L))
                .thenReturn(Optional.of(component));
        when(productBOMRepository
                .existsByProduct_ProductIdAndComponent_ProductId(1L, 2L))
                .thenReturn(false);
        when(productBOMRepository.save(any(ProductBOM.class)))
                .thenReturn(savedBom);

        BOMResponse response = bomService.addComponent(bomRequest);

        assertThat(response).isNotNull();
        assertThat(response.getProductName()).isEqualTo("UPS System");
        assertThat(response.getComponentName()).isEqualTo("Battery Cell");
        assertThat(response.getQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("Add Component - Self reference throws BadRequestException")
    void addComponent_SelfReference_ThrowsException() {
        bomRequest.setComponentId(1L); // same as productId

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> bomService.addComponent(bomRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A product cannot be a component of itself");
    }

    @Test
    @DisplayName("Add Component - Duplicate throws DuplicateResourceException")
    void addComponent_Duplicate_ThrowsException() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.findById(2L))
                .thenReturn(Optional.of(component));
        when(productBOMRepository
                .existsByProduct_ProductIdAndComponent_ProductId(1L, 2L))
                .thenReturn(true);

        assertThatThrownBy(() -> bomService.addComponent(bomRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists in the BOM");
    }

    @Test
    @DisplayName("Add Component - Product not found throws ResourceNotFoundException")
    void addComponent_ProductNotFound_ThrowsException() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bomService.addComponent(bomRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Remove Component - Not found throws ResourceNotFoundException")
    void removeComponent_NotFound_ThrowsException() {
        when(productBOMRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bomService.removeComponent(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("BOM entry not found");
    }

    @Test
    @DisplayName("Remove Component - Success")
    void removeComponent_Success() {
        when(productBOMRepository.existsById(1L)).thenReturn(true);

        bomService.removeComponent(1L);

        verify(productBOMRepository).deleteById(1L);
    }
}