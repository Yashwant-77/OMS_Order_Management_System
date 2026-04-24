package OMS_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_bom")
public class ProductBOM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productBomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Product component;

    @Column(nullable = false)
    private Integer quantity;
}