package com.bookshop.pos.repository;

import com.bookshop.pos.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    // Half-open interval [from, to) everywhere below: a sale landing exactly on a
    // boundary instant (midnight between two days/weeks) belongs to exactly one side.
    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.saleTime >= :from and s.saleTime < :to")
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countBySaleTimeGreaterThanEqualAndSaleTimeLessThan(LocalDateTime from, LocalDateTime to);

    // Per-cashier totals for the reports screen — grouped in the DB rather than
    // pulling every Sale into memory and summing in Java. Grouped by id, not name,
    // so two staff who happen to share a name never get merged into one row.
    @Query("""
            select s.user.id as userId, s.user.name as cashier,
                   coalesce(sum(s.totalAmount), 0) as total, count(s) as saleCount
            from Sale s
            where s.saleTime >= :from and s.saleTime < :to
            group by s.user.id, s.user.name
            order by total desc
            """)
    List<CashierTotalRow> cashierTotalsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Lightweight per-day breakdown for the week view's day list — total/count only,
    // grouped by calendar day in the DB, so it's one cheap query instead of running
    // the full summary() (with its top-items and per-cashier joins) once per day.
    @Query("""
            select cast(s.saleTime as date) as day,
                   coalesce(sum(s.totalAmount), 0) as total, count(s) as saleCount
            from Sale s
            where s.saleTime >= :from and s.saleTime < :to
            group by cast(s.saleTime as date)
            order by day
            """)
    List<DailyTotalRow> dailyTotalsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    interface CashierTotalRow {
        Long getUserId();
        String getCashier();
        BigDecimal getTotal();
        long getSaleCount();
    }

    interface DailyTotalRow {
        Date getDay();
        BigDecimal getTotal();
        long getSaleCount();
    }

    // Sales history header list — size(s.items) is a per-row scalar (Hibernate
    // turns it into a correlated subquery), not an aggregate, so this needs no
    // GROUP BY and no separate query per sale to get its item count.
    @Query("""
            select s.id as id, s.saleTime as saleTime, s.user.username as cashier,
                   s.totalAmount as totalAmount, size(s.items) as itemCount
            from Sale s
            where s.saleTime >= :from and s.saleTime < :to
            order by s.saleTime desc
            """)
    List<SaleHeaderRow> findHeadersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    interface SaleHeaderRow {
        Long getId();
        LocalDateTime getSaleTime();
        String getCashier();
        BigDecimal getTotalAmount();
        int getItemCount();
    }
}
