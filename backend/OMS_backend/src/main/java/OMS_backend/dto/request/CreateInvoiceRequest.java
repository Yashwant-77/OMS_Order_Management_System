package OMS_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInvoiceRequest {

    @NotNull(message = "Sales order ID is required")
    private Long salesOrderId;
}