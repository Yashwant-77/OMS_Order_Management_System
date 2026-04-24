package OMS_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BOMRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Component ID is required")
    private Long componentId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}