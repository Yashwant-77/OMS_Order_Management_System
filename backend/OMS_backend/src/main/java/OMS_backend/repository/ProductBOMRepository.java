package OMS_backend.repository;

import OMS_backend.model.ProductBOM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBOMRepository extends JpaRepository<ProductBOM, Long> {

    // get all BOM entries for a specific product
    List<ProductBOM> findByProduct_ProductId(Long productId);

    // check if this component already exists in this product's BOM
    boolean existsByProduct_ProductIdAndComponent_ProductId(
            Long productId, Long componentId);
}