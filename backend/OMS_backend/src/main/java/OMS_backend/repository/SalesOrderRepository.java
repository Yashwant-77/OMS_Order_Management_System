package OMS_backend.repository;

import OMS_backend.model.OrderStatus;
import OMS_backend.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> , JpaSpecificationExecutor<SalesOrder> {

    List<SalesOrder> findByCustomer_CustomerId(Long customerId);

    // count orders by status
    long countByStatus(OrderStatus status);

    // sum of all order amounts excluding cancelled orders
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) " + "FROM SalesOrder o WHERE o.status != 'CANCELLED'")
    double sumTotalOrderValue();
}