package OMS_backend.repository;

import OMS_backend.model.Payment;
import OMS_backend.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoice_InvoiceId(Long invoiceId);

    // get all distinct payment methods used
    @Query("SELECT DISTINCT p.paymentMethod FROM Payment p")
    List<PaymentMethod> findDistinctPaymentMethods();

    // count transactions per payment method
    long countByPaymentMethod(PaymentMethod paymentMethod);

    // sum amount per payment method
    @Query("SELECT COALESCE(SUM(p.amount), 0) " + "FROM Payment p WHERE p.paymentMethod = :method")
    double sumAmountByPaymentMethod(PaymentMethod method);

    @Query("""
SELECT MONTH(p.paymentDate) as monthNum,
       COALESCE(SUM(p.amount), 0) as totalAmount
FROM Payment p
WHERE YEAR(p.paymentDate) = :year
GROUP BY MONTH(p.paymentDate)
ORDER BY MONTH(p.paymentDate)
""")
    List<Object[]> getMonthlyPaymentTrend(int year);
}