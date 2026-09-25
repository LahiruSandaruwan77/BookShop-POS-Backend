package com.bookshop.pos.repository;

import com.bookshop.pos.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {


    @Query("""
            select i.product.id as productId, i.product.name as productName,
                   coalesce(sum(i.quantity), 0) as quantity,
                   coalesce(sum(i.lineTotal), 0) as revenue,
                   coalesce(sum(i.lineTotal - i.unitCost * i.quantity), 0) as profit
            from SaleItem i
            where i.sale.saleTime >= :from and i.sale.saleTime < :to
            group by i.product.id, i.product.name
            order by revenue desc
            """)
    List<TopItemRow> topItemsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable limit);


    @Query("""
            select coalesce(sum(i.lineTotal - i.unitCost * i.quantity), 0)
            from SaleItem i
            where i.sale.saleTime >= :from and i.sale.saleTime < :to
            """)
    BigDecimal totalProfitBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    interface TopItemRow {
        Long getProductId();
        String getProductName();
        BigDecimal getQuantity();
        BigDecimal getRevenue();
        BigDecimal getProfit();
    }
}
