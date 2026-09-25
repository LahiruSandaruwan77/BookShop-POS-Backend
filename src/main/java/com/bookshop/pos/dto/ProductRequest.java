package com.bookshop.pos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        String barcode,
        @NotBlank @Size(max = 120) String name,
        @NotNull Long categoryId,
        Long supplierId,
        @DecimalMin("0.00") BigDecimal costPrice,
        @DecimalMin("0.00") BigDecimal sellingPrice,
        @DecimalMin("0.00") BigDecimal openingStock,
        boolean service,
        boolean openPrice,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal marginPercent,
        @Min(0) Integer reorderLevel
) {}
