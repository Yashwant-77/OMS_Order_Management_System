package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyPaymentTrendReport {
    private String month;
    private int monthNumber;
    private double totalPaymentAmount;
}
