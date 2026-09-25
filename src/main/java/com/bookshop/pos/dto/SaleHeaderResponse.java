package com.bookshop.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Header-only row for the sales history list — no line items, unlike SaleResponse.
public record SaleHeaderResponse(
        Long id,
        LocalDateTime saleTime,
        String cashier,
        BigDecimal totalAmount,
        int itemCount
) {}
