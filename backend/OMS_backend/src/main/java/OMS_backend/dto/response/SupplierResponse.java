package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierResponse {
    private Long supplierId;
    private String name;
    private String email;
    private String phone;
    private String address;
}