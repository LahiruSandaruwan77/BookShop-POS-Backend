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
    // silently merge with another row under the same display name.
    @Query("""
            select i.product.id as productId, i.product.name as productName,
                   coalesce(sum(i.quantity), 0) as quantity,
                   coalesce(sum(i.lineTotal), 0) as revenue
            from SaleItem i
            where i.sale.saleTime >= :from and i.sale.saleTime < :to
            group by i.product.id, i.product.name
            order by revenue desc
            """)
    List<TopItemRow> topItemsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable limit);

    interface TopItemRow {
        Long getProductId();
        String getProductName();
        BigDecimal getQuantity();
        BigDecimal getRevenue();
    }
}
