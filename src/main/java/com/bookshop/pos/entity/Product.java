package com.bookshop.pos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, length = 32)
    private String barcode;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

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


    @Column(nullable = false, columnDefinition = "boolean default false not null")
    private boolean openPrice = false;


    @Column(precision = 5, scale = 2)
    private BigDecimal marginPercent;

    @Column(nullable = false)
    private boolean active = true;


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
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public BigDecimal getStockQty() { return stockQty; }
    public void setStockQty(BigDecimal stockQty) { this.stockQty = stockQty; }
    public boolean isService() { return service; }
    public void setService(boolean service) { this.service = service; }
    public boolean isOpenPrice() { return openPrice; }
    public void setOpenPrice(boolean openPrice) { this.openPrice = openPrice; }
    public BigDecimal getMarginPercent() { return marginPercent; }
    public void setMarginPercent(BigDecimal marginPercent) { this.marginPercent = marginPercent; }


    public boolean isStockTracked() { return !service && !openPrice; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
}
