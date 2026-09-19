package com.bookshop.pos.service;

import com.bookshop.pos.dto.ReportSummary;
import com.bookshop.pos.dto.WeekReport;
import com.bookshop.pos.repository.SaleItemRepository;
import com.bookshop.pos.repository.SaleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final int TOP_ITEMS_LIMIT = 10;

    private final SaleRepository sales;
    private final SaleItemRepository saleItems;

    public ReportService(SaleRepository sales, SaleItemRepository saleItems) {
        this.sales = sales;
        this.saleItems = saleItems;
    }

    // The one report engine: every other method here just picks a [from, to)
    // window and calls this. Total, count, top items and per-cashier totals
    // always come from the same window, so they can never disagree.
    @Transactional(readOnly = true)
    public ReportSummary summary(LocalDateTime from, LocalDateTime to) {
        var topItems = saleItems.topItemsBetween(from, to, PageRequest.of(0, TOP_ITEMS_LIMIT)).stream()
                .map(r -> new ReportSummary.TopItem(r.getProductId(), r.getProductName(), r.getQuantity(), r.getRevenue(), r.getProfit()))
                .toList();

        var cashierTotals = sales.cashierTotalsBetween(from, to).stream()
                .map(r -> new ReportSummary.CashierTotal(r.getUserId(), r.getCashier(), r.getTotal(), r.getSaleCount()))
                .toList();

        return new ReportSummary(
                from, to,
                sales.sumTotalBetween(from, to),
                sales.countBySaleTimeGreaterThanEqualAndSaleTimeLessThan(from, to),
                saleItems.totalProfitBetween(from, to),
                topItems,
                cashierTotals
        );
    }

    @Transactional(readOnly = true)
    public ReportSummary today() {
        return day(LocalDate.now());
    }

    // Whole-day boundaries, never now() — a day that hasn't finished yet just has
    // no sales past the current moment, so this needs no special-casing for "today".
    @Transactional(readOnly = true)
    public ReportSummary day(LocalDate date) {
        return summary(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    // "This week" = Monday 00:00 through the start of tomorrow — never deletes or
    // touches stored data, it's purely a date filter recomputed on every call, so
    // the displayed window just advances on its own each Monday.
    @Transactional(readOnly = true)
    public WeekReport thisWeek() {
        LocalDate now = LocalDate.now(); // captured once: a request straddling midnight must see one "today"
        LocalDate monday = now.with(DayOfWeek.MONDAY);
        LocalDateTime from = monday.atStartOfDay();
        LocalDateTime to = now.plusDays(1).atStartOfDay();

        // One cheap grouped query for the day list — not summary() run per day,
        // which would repeat the top-items/per-cashier joins for data we'd discard.
        // GROUP BY only emits a row for a day that had at least one sale, so a
        // quiet day is padded in here with zero rather than silently missing
        // from the list.
        Map<LocalDate, SaleRepository.DailyTotalRow> byDay = sales.dailyTotalsBetween(from, to).stream()
                .collect(Collectors.toMap(r -> r.getDay().toLocalDate(), r -> r));

        List<WeekReport.DaySummary> days = new ArrayList<>();
        for (LocalDate d = monday; !d.isAfter(now); d = d.plusDays(1)) {
            var row = byDay.get(d);
            days.add(new WeekReport.DaySummary(
                    d,
                    row != null ? row.getTotal() : BigDecimal.ZERO,
                    row != null ? row.getSaleCount() : 0L
            ));
        }

        // Same [from, to) window as the day list above, so the days provably sum
        // to this total — both come from summing s.totalAmount over one query's
        // worth of matching rows, just grouped differently.
        ReportSummary week = summary(from, to);

        return new WeekReport(monday, days, week);
    }
}
