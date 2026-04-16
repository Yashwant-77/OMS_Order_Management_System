package OMS_backend.repository;

import OMS_backend.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findBySupplier_SupplierId(Long supplierId);
}