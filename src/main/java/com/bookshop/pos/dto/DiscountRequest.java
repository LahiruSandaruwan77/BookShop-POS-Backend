package com.bookshop.pos.dto;

import com.bookshop.pos.entity.Discount;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DiscountRequest(
        @NotNull Long productId,
        @NotNull Discount.Type type,
        @NotNull BigDecimal amount,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
