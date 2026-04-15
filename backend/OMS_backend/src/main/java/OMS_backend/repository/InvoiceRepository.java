package OMS_backend.repository;

import OMS_backend.model.Invoice;
import OMS_backend.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsBySalesOrder_SalesOrderId(Long salesOrderId);

    Optional<Invoice> findBySalesOrder_SalesOrderId(Long salesOrderId);

    long countByStatus(InvoiceStatus status);

    // total invoiced amount
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i")
    double sumTotalInvoiced();

    // total collected (paid amount across all invoices)
    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i")
    double sumTotalCollected();
}