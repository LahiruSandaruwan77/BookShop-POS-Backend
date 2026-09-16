package com.bookshop.pos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record StockMovementRequest(
        @NotNull Long productId,
        // PURCHASE: must be positive. ADJUSTMENT: signed (e.g. -2 for damaged).
        @NotNull BigDecimal quantity,
        @NotNull Type type,
        @Size(max = 120) String note,          // required for ADJUSTMENT (checked in service)
        @DecimalMin("0.00") BigDecimal newCostPrice // optional, PURCHASE only
) {
    public enum Type { PURCHASE, ADJUSTMENT }
}
