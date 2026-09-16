package com.bookshop.pos.service;

import com.bookshop.pos.dto.ProductRequest;
import com.bookshop.pos.entity.*;
import com.bookshop.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.*;

@Service
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final StockMovementRepository movements;

    public ProductService(ProductRepository products, CategoryRepository categories,
                          StockMovementRepository movements) {
        this.products = products;
        this.categories = categories;
        this.movements = movements;
    }

    @Transactional
    public Product create(ProductRequest req) {
        String barcode = normalizeBarcode(req);
        checkDuplicateBarcode(barcode, null);
        Category category = requireCategory(req.categoryId());

        BigDecimal opening = req.service() ? null
                : (req.openingStock() == null ? BigDecimal.ZERO : req.openingStock());

        Product p = new Product(barcode, req.name().trim(), category,
                req.costPrice() == null ? BigDecimal.ZERO : req.costPrice(),
                req.sellingPrice(), opening, req.service());
        if (req.reorderLevel() != null) p.setReorderLevel(req.reorderLevel());
        p = products.save(p);

        // Opening stock goes through the movement log too — day one is auditable.
        if (!p.isService() && opening.signum() > 0) {
            movements.save(new StockMovement(p, opening,
                    StockMovement.Reason.OPENING, "Opening stock"));
        }
        return p;
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product p = require(id);
        String barcode = normalizeBarcode(req);
        checkDuplicateBarcode(barcode, id);

        p.setBarcode(barcode);
        p.setName(req.name().trim());
        p.setCategory(requireCategory(req.categoryId()));
        if (req.costPrice() != null) p.setCostPrice(req.costPrice());
        p.setSellingPrice(req.sellingPrice());
        p.setService(req.service());
        if (req.reorderLevel() != null) p.setReorderLevel(req.reorderLevel());
        // Deliberately NOT touched here: stockQty. Stock changes only via StockService.
        return p;
    }

    @Transactional
    public Product setActive(Long id, boolean active) {
        Product p = require(id);
        p.setActive(active); // soft delete — sales history stays intact
        return p;
    }

    private Product require(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
    }

    private Category requireCategory(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Unknown category"));
    }

    private String normalizeBarcode(ProductRequest req) {
        if (req.service()) return null; // services never have barcodes
        String b = req.barcode() == null ? "" : req.barcode().trim();
        return b.isEmpty() ? null : b;
    }

    private void checkDuplicateBarcode(String barcode, Long excludeId) {
        if (barcode == null) return;
        products.findByBarcodeAndActiveTrue(barcode)
                .filter(existing -> !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT,
                            "Barcode already used by \"" + existing.getName() + "\"");
                });
    }
}
