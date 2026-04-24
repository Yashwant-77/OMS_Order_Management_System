package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyOrderTrendReport {
    private String month;
    private int monthNumber;
    private long orderCount;
    private double totalOrderValue;
}
