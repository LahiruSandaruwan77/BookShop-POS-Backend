package com.bookshop.pos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime saleTime = LocalDateTime.now();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal changeGiven;

    @Column(nullable = false, length = 20)
    private String paymentMethod = "CASH";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    // Header/detail pattern: saving a Sale saves its lines too (cascade).
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    protected Sale() {}

    public Sale(AppUser user, BigDecimal totalAmount, BigDecimal paidAmount,
                BigDecimal changeGiven, String paymentMethod) {
        this.user = user;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.changeGiven = changeGiven;
        this.paymentMethod = paymentMethod;
    }

    public void addItem(SaleItem item) {
        item.setSale(this);
        items.add(item);
    }

    public Long getId() { return id; }
    public LocalDateTime getSaleTime() { return saleTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getChangeGiven() { return changeGiven; }
    public String getPaymentMethod() { return paymentMethod; }
    public AppUser getUser() { return user; }
    public List<SaleItem> getItems() { return items; }
}
