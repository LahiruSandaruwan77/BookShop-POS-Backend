package com.bookshop.pos.repository;

import com.bookshop.pos.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    // Daily sales report: everything between 00:00 and 23:59 of a day.
    List<Sale> findBySaleTimeBetween(LocalDateTime from, LocalDateTime to);

    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.saleTime between :from and :to")
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countBySaleTimeBetween(LocalDateTime from, LocalDateTime to);

    // Per-cashier totals for the reports screen — grouped in the DB rather than
    // pulling every Sale into memory and summing in Java.
    @Query("""
            select s.user.name as cashier, coalesce(sum(s.totalAmount), 0) as total, count(s) as saleCount
            from Sale s
            where s.saleTime between :from and :to
            group by s.user.name
            order by total desc
            """)
    List<CashierTotalRow> cashierTotalsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    interface CashierTotalRow {
        String getCashier();
        BigDecimal getTotal();
        long getSaleCount();
    }
}
