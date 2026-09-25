package com.bookshop.pos.dto;

import com.bookshop.pos.entity.Sale;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        LocalDateTime saleTime,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal changeGiven,
        String paymentMethod,
        String cashier,
        List<Line> items
) {

    public record Line(String name, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal,
                        BigDecimal originalUnitPrice, BigDecimal discountAmount) {}

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleTime(),
                sale.getTotalAmount(),
                sale.getPaidAmount(),
                sale.getChangeGiven(),
                sale.getPaymentMethod(),
                sale.getUser().getUsername(),
                sale.getItems().stream()
                        .map(i -> new Line(i.getProduct().getName(), i.getQuantity(),
                                i.getUnitPrice(), i.getLineTotal(),
                                i.getOriginalUnitPrice(), i.getDiscountAmount()))
                        .toList()
        );
    }
}
