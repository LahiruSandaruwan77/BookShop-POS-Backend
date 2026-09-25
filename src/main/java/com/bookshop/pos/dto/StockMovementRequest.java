package com.bookshop.pos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record StockMovementRequest(
        @NotNull Long productId,
        @NotNull BigDecimal quantity,
        @NotNull Type type,
        @Size(max = 120) String note,
        @DecimalMin("0.00") BigDecimal newCostPrice
) {
    public enum Type { PURCHASE, ADJUSTMENT }
}
