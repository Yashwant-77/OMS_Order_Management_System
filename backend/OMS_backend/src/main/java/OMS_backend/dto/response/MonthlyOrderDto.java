package OMS_backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyOrderDto {
    private String month;
    private long count;


}
