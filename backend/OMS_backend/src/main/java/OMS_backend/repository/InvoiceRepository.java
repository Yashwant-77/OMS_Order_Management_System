package OMS_backend.repository;

import OMS_backend.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // check if invoice already exists for this sales order
    boolean existsBySalesOrder_SalesOrderId(Long salesOrderId);

    // get invoice by sales order id
    Optional<Invoice> findBySalesOrder_SalesOrderId(Long salesOrderId);
}