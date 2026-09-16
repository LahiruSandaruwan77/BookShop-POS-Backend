package com.bookshop.pos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable: loose pens and services have no barcode. Unique when present.
    @Column(unique = true, length = 32)
    private String barcode;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    // ALWAYS BigDecimal for money — double/float cause rounding bugs on bills.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    // Decimal so we can sell fractional units later (by weight etc). Null for services.
    @Column(precision = 10, scale = 2)
    private BigDecimal stockQty;

    // Services (photocopy, printout) skip stock deduction entirely.
    @Column(nullable = false)
    private boolean service = false;

    // Soft delete: never remove a product that old sales reference.
    @Column(nullable = false)
    private boolean active = true;

    // Per-item low-stock threshold ("low" for pens != "low" for novels).
    @Column(nullable = false)
    private int reorderLevel = 10;

    protected Product() {}

    public Product(String barcode, String name, Category category,
                   BigDecimal costPrice, BigDecimal sellingPrice,
                   BigDecimal stockQty, boolean service) {
        this.barcode = barcode;
        this.name = name;
        this.category = category;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stockQty = stockQty;
        this.service = service;
    }

    public Long getId() { return id; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public BigDecimal getStockQty() { return stockQty; }
    public void setStockQty(BigDecimal stockQty) { this.stockQty = stockQty; }
    public boolean isService() { return service; }
    public void setService(boolean service) { this.service = service; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
}
