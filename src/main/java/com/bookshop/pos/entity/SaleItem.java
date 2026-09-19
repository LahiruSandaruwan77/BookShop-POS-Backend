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

    // Price SNAPSHOT at sale time — never look it up from Product for old bills,
    // or history changes retroactively when prices change.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Cost SNAPSHOT at sale time, same principle as unitPrice — profit reports
    // must reflect what the item actually cost when it was sold, not today's cost.
    // columnDefinition carries "default 0" so this is safe to add to a database
    // that already has sale_items rows: Hibernate's ddl-auto=update issues an
    // ALTER TABLE ... ADD COLUMN ... DEFAULT 0 NOT NULL, so existing rows get
    // unitCost = 0 instead of violating the not-null constraint. That 0 means
    // "recorded before cost tracking existed," not a real zero cost — read
    // profit figures from before this column existed with that caveat.
    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "decimal(10,2) default 0 not null")
    private BigDecimal unitCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected SaleItem() {}

    public SaleItem(Product product, BigDecimal quantity, BigDecimal unitPrice, BigDecimal unitCost) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitCost = unitCost;
        this.lineTotal = unitPrice.multiply(quantity);
    }

    public Long getId() { return id; }
    public Sale getSale() { return sale; }
    void setSale(Sale sale) { this.sale = sale; }
    public Product getProduct() { return product; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
