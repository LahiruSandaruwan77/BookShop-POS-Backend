package com.bookshop.pos.service;

import com.bookshop.pos.dto.DiscountRequest;
import com.bookshop.pos.entity.Discount;
import com.bookshop.pos.entity.Product;
import com.bookshop.pos.repository.DiscountRepository;
import com.bookshop.pos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class DiscountService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final DiscountRepository discounts;
    private final ProductRepository products;

    public DiscountService(DiscountRepository discounts, ProductRepository products) {
        this.discounts = discounts;
        this.products = products;
    }

    @Transactional
    public Discount create(DiscountRequest req) {
        Product p = products.findById(req.productId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Unknown product"));


        if (p.isService() || p.isOpenPrice()) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Discounts apply only to normal products, not services or open-price items");
        }

        if (!req.endDate().isAfter(req.startDate())) {
            throw new ResponseStatusException(BAD_REQUEST, "End date must be after start date");
        }

        validateValue(req, p);


        boolean overlaps = discounts.findByProductIdAndActiveTrue(p.getId()).stream()
                .anyMatch(d -> req.startDate().isBefore(d.getEndDate()) && d.getStartDate().isBefore(req.endDate()));
        if (overlaps) {
            throw new ResponseStatusException(CONFLICT,
                    "This product already has a discount during that period — turn it off first.");
        }

        return discounts.save(new Discount(p, req.type(), req.amount(), req.startDate(), req.endDate()));
    }

    private void validateValue(DiscountRequest req, Product p) {
        if (req.type() == Discount.Type.PERCENT) {
            if (req.amount().compareTo(BigDecimal.ZERO) < 0 || req.amount().compareTo(HUNDRED) > 0) {
                throw new ResponseStatusException(BAD_REQUEST, "Percent discount must be between 0 and 100");
            }
        } else {
            if (req.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(BAD_REQUEST, "Fixed discount must be greater than zero");
            }
            if (req.amount().compareTo(p.getSellingPrice()) > 0) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "Fixed discount can't exceed the product's selling price (" + p.getSellingPrice() + ")");
            }
        }
    }

    @Transactional
    public Discount setActive(Long id, boolean active) {
        Discount d = discounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Discount not found"));
        d.setActive(active);
        return d;
    }

    @Transactional(readOnly = true)
    public List<Discount> list() {
        return discounts.findAllByOrderByStartDateDesc();
    }
}
