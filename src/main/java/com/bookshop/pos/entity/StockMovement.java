package com.bookshop.pos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {


    public enum Reason { OPENING, PURCHASE, SALE, ADJUSTMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal qtyChange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Reason reason;

    @Column(length = 120)
    private String note;

    @Column(nullable = false)
    private LocalDateTime movedAt = LocalDateTime.now();

    protected StockMovement() {}

    public StockMovement(Product product, BigDecimal qtyChange, Reason reason, String note) {
        this.product = product;
        this.qtyChange = qtyChange;
        this.reason = reason;
        this.note = note;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public BigDecimal getQtyChange() { return qtyChange; }
    public Reason getReason() { return reason; }
    public String getNote() { return note; }
    public LocalDateTime getMovedAt() { return movedAt; }
}
