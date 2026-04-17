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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BOMService {

    private final ProductBOMRepository productBOMRepository;
    private final ProductRepository productRepository;

    public BOMResponse addComponent(BOMRequest request) {

        log.info("Received request to add component. productId={}, componentId={}, quantity={}",
                request.getProductId(), request.getComponentId(), request.getQuantity());

        // validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found with id={}", request.getProductId());
                    return new ResourceNotFoundException("Product not found with id: " + request.getProductId());
                });

        // validate component exists
        Product component = productRepository.findById(request.getComponentId())
                .orElseThrow(() -> {
                    log.error("Component not found with id={}", request.getComponentId());
                    return new ResourceNotFoundException("Component not found with id: " + request.getComponentId());
                });

        // a product cannot be its own component
        if (request.getProductId().equals(request.getComponentId())) {
            log.warn("Invalid request: product cannot be its own component. productId={}", request.getProductId());
            throw new BadRequestException("A product cannot be a component of itself");
        }

        // prevent duplicate component in same BOM
        if (productBOMRepository.existsByProduct_ProductIdAndComponent_ProductId(
                request.getProductId(), request.getComponentId())) {

            log.warn("Duplicate BOM entry detected for productId={} and componentId={}",
                    request.getProductId(), request.getComponentId());

            throw new DuplicateResourceException("This component already exists in the BOM for this product");
        }

        ProductBOM bom = new ProductBOM();
        bom.setProduct(product);
        bom.setComponent(component);
        bom.setQuantity(request.getQuantity());

        ProductBOM savedBom = productBOMRepository.save(bom);

        log.info("Successfully added component to BOM. bomId={}, productId={}, componentId={}",
                savedBom.getProductBomId(),
                request.getProductId(),
                request.getComponentId());

        return mapToResponse(savedBom);
    }

    @Transactional(readOnly = true)
    public List<BOMResponse> getBOMByProductId(Long productId) {

        log.info("Fetching BOM for productId={}", productId);

        // validate product exists
        if (!productRepository.existsById(productId)) {
            log.error("Product not found while fetching BOM. productId={}", productId);
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<BOMResponse> response = productBOMRepository.findByProduct_ProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} BOM entries for productId={}", response.size(), productId);

        return response;
    }

    @Transactional
    public void removeComponent(Long bomId) {

        log.info("Request received to delete BOM entry. bomId={}", bomId);

        if (!productBOMRepository.existsById(bomId)) {
            log.error("BOM entry not found. bomId={}", bomId);
            throw new ResourceNotFoundException("BOM entry not found with id: " + bomId);
        }

        productBOMRepository.deleteById(bomId);

        log.info("Successfully deleted BOM entry. bomId={}", bomId);
    }

    // helper method to map ProductBOM to BOMResponse
    private BOMResponse mapToResponse(ProductBOM bom) {

        log.debug("Mapping ProductBOM to BOMResponse. bomId={}", bom.getProductBomId());

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