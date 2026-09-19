package com.bookshop.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WeekReport(
        LocalDate weekStart,
        List<DaySummary> days,
        ReportSummary week
) {
    public record DaySummary(LocalDate date, BigDecimal totalSales, long saleCount) {}
}
