package com.bookshop.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReportSummary(
        LocalDateTime from,
        LocalDateTime to,
        BigDecimal totalSales,
        long saleCount,
        List<TopItem> topItems,
        List<CashierTotal> cashierTotals
) {
    public record TopItem(Long productId, String productName, BigDecimal quantity, BigDecimal revenue) {}

    public record CashierTotal(Long userId, String cashier, BigDecimal total, long saleCount) {}
}
