package com.bookshop.pos.dto;

import com.bookshop.pos.entity.Product;
import java.math.BigDecimal;


public record ProductResponse(
        Long id,
        String barcode,
        String name,
        String category,
        Long supplierId,
        String supplierName,
        BigDecimal costPrice,
        BigDecimal sellingPrice,
        BigDecimal stockQty,
        boolean service,
        boolean openPrice,
        BigDecimal marginPercent,
        boolean active,
        int reorderLevel
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getBarcode(), p.getName(),
                p.getCategory().getName(),
                p.getSupplier() == null ? null : p.getSupplier().getId(),
                p.getSupplier() == null ? null : p.getSupplier().getName(),
                p.getCostPrice(), p.getSellingPrice(), p.getStockQty(),
                p.isService(), p.isOpenPrice(), p.getMarginPercent(),
                p.isActive(), p.getReorderLevel()
        );
    }
}
