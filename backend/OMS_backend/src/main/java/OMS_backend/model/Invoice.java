package OMS_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    // one sales order => one invoice
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false, unique = true)
    private SalesOrder salesOrder;

    @Column(nullable = false)
    private LocalDateTime invoiceDate;

    @Column(nullable = false)
    private Double totalAmount;

    // tracks how much has been paid so far
    @Column(nullable = false)
    private Double paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;
}