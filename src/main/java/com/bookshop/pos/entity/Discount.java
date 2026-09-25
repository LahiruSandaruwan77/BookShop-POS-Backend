package com.bookshop.pos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "discounts")
public class Discount {

    public enum Type { PERCENT, FIXED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Type type;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    protected Discount() {}

    public Discount(Product product, Type type, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        this.product = product;
        this.type = type;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isActiveOn(LocalDate date) {
        return active && !date.isBefore(startDate) && date.isBefore(endDate);
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public Type getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
