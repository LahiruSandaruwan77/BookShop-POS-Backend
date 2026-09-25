package com.bookshop.pos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "decimal(10,2) default 0 not null")
    private BigDecimal unitCost;

    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "decimal(10,2) default 0 not null")
    private BigDecimal originalUnitPrice;

    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "decimal(10,2) default 0 not null")
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected SaleItem() {}

    public SaleItem(Product product, BigDecimal quantity, BigDecimal unitPrice, BigDecimal unitCost,
                     BigDecimal originalUnitPrice, BigDecimal discountAmount) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitCost = unitCost;
        this.originalUnitPrice = originalUnitPrice;
        this.discountAmount = discountAmount;
        this.lineTotal = unitPrice.multiply(quantity);
    }

    public Long getId() { return id; }
    public Sale getSale() { return sale; }
    void setSale(Sale sale) { this.sale = sale; }
    public Product getProduct() { return product; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getOriginalUnitPrice() { return originalUnitPrice; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
