package OMS_backend.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreatePurchaseOrderRequest {
    private Long supplierId;
    private List<PurchaseOrderItemRequest> items;
}