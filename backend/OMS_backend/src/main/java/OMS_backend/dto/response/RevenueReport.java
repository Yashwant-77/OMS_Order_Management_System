package OMS_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RevenueReport {
    private double totalInvoiced;
    private double totalCollected;
    private double totalOutstanding;
    private long totalInvoices;
    private long paidInvoices;
    private long partiallyPaidInvoices;
    private long pendingInvoices;
}