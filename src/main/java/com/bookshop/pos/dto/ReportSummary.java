package com.bookshop.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReportSummary(
        String range,
        LocalDateTime from,
        LocalDateTime to,
        BigDecimal totalSales,
        long saleCount,
        List<TopItem> topItems,
        List<CashierTotal> cashierTotals
) {
    public record TopItem(String productName, BigDecimal quantity, BigDecimal revenue) {}

    public record CashierTotal(String cashier, BigDecimal total, long saleCount) {}
}
