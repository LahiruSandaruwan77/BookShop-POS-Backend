package com.bookshop.pos.service;

import com.bookshop.pos.dto.ReportSummary;
import com.bookshop.pos.repository.SaleItemRepository;
import com.bookshop.pos.repository.SaleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ReportService {

    private static final int TOP_ITEMS_LIMIT = 10;

    private final SaleRepository sales;
    private final SaleItemRepository saleItems;

    public ReportService(SaleRepository sales, SaleItemRepository saleItems) {
        this.sales = sales;
        this.saleItems = saleItems;
    }

    @Transactional(readOnly = true)
    public ReportSummary summary(String range) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = switch (range) {
            case "today" -> LocalDate.now().atStartOfDay();
            // Rolling 7-day window (today + previous 6 days) rather than a Mon-Sun
            // calendar week — avoids a week-start convention nobody agreed on.
            case "week" -> LocalDate.now().minusDays(6).atStartOfDay();
            default -> throw new ResponseStatusException(BAD_REQUEST,
                    "Unknown range \"" + range + "\" — use \"today\" or \"week\"");
        };

        var topItems = saleItems.topItemsBetween(from, to, PageRequest.of(0, TOP_ITEMS_LIMIT)).stream()
                .map(r -> new ReportSummary.TopItem(r.getProductName(), r.getQuantity(), r.getRevenue()))
                .toList();

        var cashierTotals = sales.cashierTotalsBetween(from, to).stream()
                .map(r -> new ReportSummary.CashierTotal(r.getCashier(), r.getTotal(), r.getSaleCount()))
                .toList();

        return new ReportSummary(
                range, from, to,
                sales.sumTotalBetween(from, to),
                sales.countBySaleTimeBetween(from, to),
                topItems,
                cashierTotals
        );
    }
}
