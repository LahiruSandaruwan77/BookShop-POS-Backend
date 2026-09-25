package com.bookshop.pos.dto;

import com.bookshop.pos.entity.Discount;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DiscountResponse(
        Long id,
        Long productId,
        String productName,
        Discount.Type type,
        BigDecimal amount,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String status
) {
    public static DiscountResponse from(Discount d) {
        LocalDate today = LocalDate.now();
        String status = today.isBefore(d.getStartDate()) ? "upcoming"
                : today.isBefore(d.getEndDate()) ? "active"
                : "expired";
        return new DiscountResponse(
                d.getId(), d.getProduct().getId(), d.getProduct().getName(),
                d.getType(), d.getAmount(), d.getStartDate(), d.getEndDate(),
                d.isActive(), status
        );
    }
}
