package com.bookshop.pos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * What the billing screen sends. Note what is NOT here: prices and totals.
 * The client only says WHAT and HOW MANY — the server looks up prices and
 * computes totals itself. Never trust money math done in the browser.
 */
public record SaleRequest(
        @NotEmpty(message = "A sale needs at least one item")
        @Valid List<Line> items,

        @NotNull @DecimalMin(value = "0.00")
        BigDecimal paidAmount,

        String paymentMethod // defaults to CASH if null
) {
    public record Line(
            @NotNull Long productId,
            @NotNull @DecimalMin(value = "0.01", message = "Quantity must be positive")
            BigDecimal quantity
    ) {}
}
