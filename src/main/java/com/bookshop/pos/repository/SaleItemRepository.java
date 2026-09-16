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

    // Top-selling items for the reports screen, ranked by revenue.
    @Query("""
            select i.product.name as productName,
                   coalesce(sum(i.quantity), 0) as quantity,
                   coalesce(sum(i.lineTotal), 0) as revenue
            from SaleItem i
            where i.sale.saleTime between :from and :to
            group by i.product.name
            order by revenue desc
            """)
    List<TopItemRow> topItemsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable limit);

    interface TopItemRow {
        String getProductName();
        BigDecimal getQuantity();
        BigDecimal getRevenue();
    }
}
