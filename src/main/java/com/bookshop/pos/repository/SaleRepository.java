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


    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.saleTime >= :from and s.saleTime < :to")
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countBySaleTimeGreaterThanEqualAndSaleTimeLessThan(LocalDateTime from, LocalDateTime to);


    @Query("""
            select s.user.id as userId, s.user.name as cashier,
                   coalesce(sum(s.totalAmount), 0) as total, count(s) as saleCount
            from Sale s
            where s.saleTime >= :from and s.saleTime < :to
            group by s.user.id, s.user.name
            order by total desc
            """)
    List<CashierTotalRow> cashierTotalsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);


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
