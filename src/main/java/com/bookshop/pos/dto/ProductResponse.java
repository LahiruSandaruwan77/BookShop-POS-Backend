package com.bookshop.pos.dto;

import com.bookshop.pos.entity.Product;
import java.math.BigDecimal;

/**
 * DTO instead of returning the entity directly: controls exactly what the
 * frontend sees and avoids lazy-loading surprises with the category relation.
 */
public record ProductResponse(
        Long id,
        String barcode,
        String name,
        String category,
        BigDecimal costPrice,
        BigDecimal sellingPrice,
        BigDecimal stockQty,
        boolean service,
        boolean active,
        int reorderLevel
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getBarcode(), p.getName(),
                p.getCategory().getName(),
                p.getCostPrice(), p.getSellingPrice(), p.getStockQty(),
                p.isService(), p.isActive(), p.getReorderLevel()
        );
    }
}
