package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LowStockReport {
    private Long productId;
    private String productName;
    private Integer quantityInStock;
    private Double unitPrice;
}