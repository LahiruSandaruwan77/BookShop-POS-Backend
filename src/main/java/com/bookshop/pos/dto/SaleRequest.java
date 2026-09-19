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
            BigDecimal quantity,

            // Only for open-price products, where the cashier enters the price at
            // billing time. NOT @NotNull: whether it's required depends on the
            // product's type, which only the server (via a DB lookup) can know —
            // SaleService validates it, not this DTO. For any other product type
            // this is read and then ignored, never compared against the fixed price.
            BigDecimal unitPrice
    ) {}
}
