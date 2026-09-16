package com.bookshop.pos.dto;

import com.bookshop.pos.entity.StockMovement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id, String product, BigDecimal qtyChange,
        String reason, String note, LocalDateTime movedAt
) {
    public static StockMovementResponse from(StockMovement m) {
        return new StockMovementResponse(
                m.getId(), m.getProduct().getName(), m.getQtyChange(),
                m.getReason().name(), m.getNote(), m.getMovedAt());
    }
}
