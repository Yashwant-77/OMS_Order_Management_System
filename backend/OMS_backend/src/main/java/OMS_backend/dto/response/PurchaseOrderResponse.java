package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long purchaseOrderId;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private List<PurchaseOrderItemResponse> items;
}