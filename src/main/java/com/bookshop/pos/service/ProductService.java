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
    private final SupplierRepository suppliers;
    private final StockMovementRepository movements;

    public ProductService(ProductRepository products, CategoryRepository categories,
                          SupplierRepository suppliers, StockMovementRepository movements) {
        this.products = products;
        this.categories = categories;
        this.suppliers = suppliers;
        this.movements = movements;
    }

    @Transactional
    public Product create(ProductRequest req) {
        validateType(req);
        String barcode = normalizeBarcode(req);
        checkDuplicateBarcode(barcode, null);
        Category category = requireCategory(req.categoryId());

        Product p = new Product(barcode, req.name().trim(), category,
                req.costPrice() == null ? BigDecimal.ZERO : req.costPrice(),
                sellingPriceFor(req), null, req.service());
        p.setSupplier(requireSupplierOrNull(req.supplierId()));
        p.setOpenPrice(req.openPrice());
        if (req.openPrice()) p.setMarginPercent(req.marginPercent() == null ? BigDecimal.ZERO : req.marginPercent());
        if (req.reorderLevel() != null) p.setReorderLevel(req.reorderLevel());


        BigDecimal opening = p.isStockTracked()
                ? (req.openingStock() == null ? BigDecimal.ZERO : req.openingStock())
                : null;
        p.setStockQty(opening);
        p = products.save(p);


        if (p.isStockTracked() && opening.signum() > 0) {
            movements.save(new StockMovement(p, opening,
                    StockMovement.Reason.OPENING, "Opening stock"));
        }
        return p;
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        validateType(req);
        Product p = require(id);
        String barcode = normalizeBarcode(req);
        checkDuplicateBarcode(barcode, id);

        p.setBarcode(barcode);
        p.setName(req.name().trim());
        p.setCategory(requireCategory(req.categoryId()));
        p.setSupplier(requireSupplierOrNull(req.supplierId()));
        if (req.costPrice() != null) p.setCostPrice(req.costPrice());
        p.setSellingPrice(sellingPriceFor(req));
        p.setService(req.service());
        p.setOpenPrice(req.openPrice());
        p.setMarginPercent(req.openPrice() ? (req.marginPercent() == null ? BigDecimal.ZERO : req.marginPercent()) : null);
        if (req.reorderLevel() != null) p.setReorderLevel(req.reorderLevel());
        return p;
    }

    // A product is normal, service, or open-price — never more than one.
    private void validateType(ProductRequest req) {
        if (req.service() && req.openPrice()) {
            throw new ResponseStatusException(BAD_REQUEST, "A product can't be both a service and open-price");
        }
    }

    // Open-price products have no fixed selling price — the cashier sets it per
    // sale (see SaleService), so this is just a harmless placeholder that's
    // never read for pricing.
    private BigDecimal sellingPriceFor(ProductRequest req) {
        if (req.openPrice()) return BigDecimal.ZERO;
        return req.sellingPrice() == null ? BigDecimal.ZERO : req.sellingPrice();
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

    // Supplier is optional on a product — null id means no supplier, a non-null
    // id that doesn't exist is a client error.
    private Supplier requireSupplierOrNull(Long id) {
        if (id == null) return null;
        return suppliers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Unknown supplier"));
    }

    private String normalizeBarcode(ProductRequest req) {
        if (req.service() || req.openPrice()) return null; // neither ever has a barcode
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
