package com.bookshop.pos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        String barcode,                       // null/blank for services & open-price items
        @NotBlank @Size(max = 120) String name,
        @NotNull Long categoryId,
        @DecimalMin("0.00") BigDecimal costPrice,
        // Not @NotNull: meaningless for an open-price product (the cashier sets
        // the price per sale) — ProductService defaults it to zero when absent.
        @DecimalMin("0.00") BigDecimal sellingPrice,
        @DecimalMin("0.00") BigDecimal openingStock, // used on create only
        boolean service,
        boolean openPrice,                    // mutually exclusive with service — see ProductService
        // Meaningful only when openPrice is true: margin as % of the entered
        // selling price. A simple, unconditional numeric bound, so it's checked
        // here rather than in the service (unlike SaleRequest.Line.unitPrice,
        // whose requiredness depends on a DB lookup a DTO can't do).
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal marginPercent,
        @Min(0) Integer reorderLevel
) {}
