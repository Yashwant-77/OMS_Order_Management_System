package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BOMResponse {
    private Long productBomId;
    private Long productId;
    private String productName;
    private Long componentId;
    private String componentName;
    private Integer quantity;
}