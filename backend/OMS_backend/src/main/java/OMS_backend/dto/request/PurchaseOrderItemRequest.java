package OMS_backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderItemRequest {
    private Long productId;
    private Integer quantity;
    private Double unitPrice;  // negotiated price with supplier
}