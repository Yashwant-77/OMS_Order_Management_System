package OMS_backend.service;

import OMS_backend.dto.request.BOMRequest;
import OMS_backend.dto.response.BOMResponse;
import OMS_backend.exception.DuplicateResourceException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.Product;
import OMS_backend.model.ProductBOM;
import OMS_backend.repository.ProductBOMRepository;
import OMS_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BOMService {

    private final ProductBOMRepository productBOMRepository;

    private final ProductRepository productRepository;

    // add component to BOM
    public BOMResponse addComponent(BOMRequest request) {

        //validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        // validate component exists
        Product component = productRepository.findById(request.getComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("Component not found with id: " + request.getComponentId()));

        // a product cannot be its own component
        if (request.getProductId().equals(request.getComponentId())) {
            throw new RuntimeException("A product cannot be a component of itself");
        }

        // prevent duplicate component in same BOM
        if (productBOMRepository.existsByProduct_ProductIdAndComponent_ProductId(
                request.getProductId(), request.getComponentId())) {
            throw new DuplicateResourceException("This component already exists in the BOM for this product");
        }

        // save
        ProductBOM bom = new ProductBOM();
        bom.setProduct(product);
        bom.setComponent(component);
        bom.setQuantity(request.getQuantity());

        return mapToResponse(productBOMRepository.save(bom));
    }

    // get BOM by product id
    public List<BOMResponse> getBOMByProductId(Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return productBOMRepository.findByProduct_ProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // remove component from BOM
    public void removeComponent(Long bomId) {
        if (!productBOMRepository.existsById(bomId)) {
            throw new ResourceNotFoundException("BOM entry not found with id: " + bomId);
        }
        productBOMRepository.deleteById(bomId);
    }

    // mapping ProductBOM to BOMResponse
    private BOMResponse mapToResponse(ProductBOM bom) {
        return new BOMResponse(
                bom.getProductBomId(),
                bom.getProduct().getProductId(),
                bom.getProduct().getProductName(),
                bom.getComponent().getProductId(),
                bom.getComponent().getProductName(),
                bom.getQuantity()
        );
    }
}