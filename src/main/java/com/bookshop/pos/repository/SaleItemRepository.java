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

    // Top-selling items for the reports screen, ranked by revenue. Grouped by
    // product id (name along for display) so a renamed/re-added product can't
    // silently merge with another row under the same display name. Profit per
    // item = revenue minus the cost snapshotted at sale time (see SaleItem.unitCost).
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

    // Same half-open interval and coalesce-to-zero convention as the other
    // report aggregates. Profit = sum of (lineTotal - unitCost * quantity)
    // over every matching line, not a difference of two separately-summed totals.
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
