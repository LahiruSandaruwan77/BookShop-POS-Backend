package com.bookshop.pos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        String barcode,                       // null/blank for services & loose items
        @NotBlank @Size(max = 120) String name,
        @NotNull Long categoryId,
        @DecimalMin("0.00") BigDecimal costPrice,
        @NotNull @DecimalMin("0.00") BigDecimal sellingPrice,
        @DecimalMin("0.00") BigDecimal openingStock, // used on create only
        boolean service,
        @Min(0) Integer reorderLevel
) {}
