package com.bookshop.pos.service;

import com.bookshop.pos.dto.SaleHeaderResponse;
import com.bookshop.pos.dto.SaleRequest;
import com.bookshop.pos.entity.*;
import com.bookshop.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class SaleService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal MAX_OPEN_PRICE = new BigDecimal("100000");

    private final SaleRepository sales;
    private final ProductRepository products;
    private final DiscountRepository discounts;
    private final StockMovementRepository movements;
    private final AppUserRepository users;

    public SaleService(SaleRepository sales, ProductRepository products, DiscountRepository discounts,
                       StockMovementRepository movements, AppUserRepository users) {
        this.sales = sales;
        this.products = products;
        this.discounts = discounts;
        this.movements = movements;
        this.users = users;
    }


    private record PricedLine(BigDecimal originalPrice, BigDecimal finalPrice) {}


    @Transactional
    public Sale checkout(SaleRequest req, String cashierUsername) {
        AppUser cashier = users.findByUsernameIgnoreCase(cashierUsername)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unknown cashier"));
        LocalDate today = LocalDate.now();

        BigDecimal total = BigDecimal.ZERO;
        for (SaleRequest.Line line : req.items()) {
            Product p = products.findById(line.productId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Unknown or inactive product: id " + line.productId()));

            if (p.isStockTracked() && p.getStockQty().compareTo(line.quantity()) < 0) {
                throw new ResponseStatusException(CONFLICT,
                        "Not enough stock for \"" + p.getName() + "\" — have "
                                + p.getStockQty() + ", need " + line.quantity());
            }
            total = total.add(resolveUnitPrice(p, line, today).finalPrice().multiply(line.quantity()));
        }

        if (req.paidAmount().compareTo(total) < 0) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Paid amount " + req.paidAmount() + " is less than total " + total);
        }

        String method = req.paymentMethod() == null ? "CASH" : req.paymentMethod();
        Sale sale = new Sale(cashier, total, req.paidAmount(),
                req.paidAmount().subtract(total), method);

        for (SaleRequest.Line line : req.items()) {
            Product p = products.findById(line.productId()).orElseThrow(); // validated above
            PricedLine priced = resolveUnitPrice(p, line, today);
            BigDecimal cost = resolveUnitCost(p, priced.finalPrice());
            BigDecimal discountAmount = priced.originalPrice().subtract(priced.finalPrice());
            sale.addItem(new SaleItem(p, line.quantity(), priced.finalPrice(), cost,
                    priced.originalPrice(), discountAmount));

            if (p.isStockTracked()) { // services & open-price items skip inventory
                p.setStockQty(p.getStockQty().subtract(line.quantity()));
                movements.save(new StockMovement(p, line.quantity().negate(),
                        StockMovement.Reason.SALE, "Sale"));
            }
        }

        return sales.save(sale); // cascade saves all SaleItems too
    }

    private PricedLine resolveUnitPrice(Product p, SaleRequest.Line line, LocalDate today) {
        if (p.isOpenPrice()) {
            BigDecimal entered = line.unitPrice();
            if (entered == null) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "Price required for open-price item: " + p.getName());
            }
            if (entered.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "Price must be positive for open-price item: " + p.getName());
            }
            if (entered.compareTo(MAX_OPEN_PRICE) > 0) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "Price too high for open-price item: " + p.getName() + " (" + entered + ")");
            }
            BigDecimal price = entered.setScale(2, RoundingMode.HALF_UP);
            return new PricedLine(price, price); // open-price items can't have discounts (see DiscountService)
        }

        BigDecimal original = p.getSellingPrice();
        if (p.isService()) {
            return new PricedLine(original, original); // services can't have discounts either
        }


        BigDecimal discounted = discounts.findActiveForProductOn(p.getId(), today)
                .map(d -> applyDiscount(original, d))
                .orElse(original);
        return new PricedLine(original, discounted);
    }

    private BigDecimal applyDiscount(BigDecimal price, Discount d) {
        BigDecimal reduced = d.getType() == Discount.Type.PERCENT
                ? price.subtract(price.multiply(d.getAmount()).divide(HUNDRED, 2, RoundingMode.HALF_UP))
                : price.subtract(d.getAmount());
        return reduced.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP); // never below zero
    }


    private BigDecimal resolveUnitCost(Product p, BigDecimal unitPrice) {
        if (p.isOpenPrice()) {
            // profit = enteredPrice x margin%; cost = enteredPrice - profit.
            // Null margin (never configured) means zero profit, not a crash.
            BigDecimal marginPercent = p.getMarginPercent() == null ? BigDecimal.ZERO : p.getMarginPercent();
            BigDecimal profit = unitPrice.multiply(marginPercent)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            return unitPrice.subtract(profit).setScale(2, RoundingMode.HALF_UP);
        }
        if (p.isService()) {
            return BigDecimal.ZERO; // services have no goods cost — their whole price is margin
        }
        return p.getCostPrice();
    }

    @Transactional(readOnly = true)
    public Sale get(Long id) {
        Sale sale = sales.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sale not found"));
        sale.getItems().size(); // touch lazy collection inside the transaction
        return sale;
    }


    @Transactional(readOnly = true)
    public List<SaleHeaderResponse> listBetween(LocalDateTime from, LocalDateTime to) {
        return sales.findHeadersBetween(from, to).stream()
                .map(r -> new SaleHeaderResponse(r.getId(), r.getSaleTime(), r.getCashier(), r.getTotalAmount(), r.getItemCount()))
                .toList();
    }
}
