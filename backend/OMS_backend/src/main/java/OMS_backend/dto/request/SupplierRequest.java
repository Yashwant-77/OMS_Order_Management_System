package OMS_backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
}