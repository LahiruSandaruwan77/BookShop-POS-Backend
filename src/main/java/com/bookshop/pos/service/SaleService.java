package com.bookshop.pos.service;

import com.bookshop.pos.dto.SaleRequest;
import com.bookshop.pos.entity.*;
import com.bookshop.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.*;

@Service
public class SaleService {

    private final SaleRepository sales;
    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final AppUserRepository users;

    public SaleService(SaleRepository sales, ProductRepository products,
                       StockMovementRepository movements, AppUserRepository users) {
        this.sales = sales;
        this.products = products;
        this.movements = movements;
        this.users = users;
    }

    @Transactional
    public Sale checkout(SaleRequest req, String cashierUsername) {
        AppUser cashier = users.findByUsernameIgnoreCase(cashierUsername)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unknown cashier"));


        BigDecimal total = BigDecimal.ZERO;
        for (SaleRequest.Line line : req.items()) {
            Product p = products.findById(line.productId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Unknown or inactive product: id " + line.productId()));

            if (!p.isService() && p.getStockQty().compareTo(line.quantity()) < 0) {
                throw new ResponseStatusException(CONFLICT,
                        "Not enough stock for \"" + p.getName() + "\" — have "
                                + p.getStockQty() + ", need " + line.quantity());
            }
            total = total.add(p.getSellingPrice().multiply(line.quantity()));
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
            sale.addItem(new SaleItem(p, line.quantity(), p.getSellingPrice()));

            if (!p.isService()) { 
                p.setStockQty(p.getStockQty().subtract(line.quantity()));
                movements.save(new StockMovement(p, line.quantity().negate(),
                        StockMovement.Reason.SALE, "Sale"));
            }
        }

        return sales.save(sale);
    }

    @Transactional(readOnly = true)
    public Sale get(Long id) {
        Sale sale = sales.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sale not found"));
        sale.getItems().size();
        return sale;
    }
}
