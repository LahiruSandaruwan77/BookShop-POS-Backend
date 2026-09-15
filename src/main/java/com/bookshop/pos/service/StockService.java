package com.bookshop.pos.service;

import com.bookshop.pos.dto.StockMovementRequest;
import com.bookshop.pos.entity.Product;
import com.bookshop.pos.entity.StockMovement;
import com.bookshop.pos.repository.ProductRepository;
import com.bookshop.pos.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.*;

@Service
public class StockService {

    private final ProductRepository products;
    private final StockMovementRepository movements;

    public StockService(ProductRepository products, StockMovementRepository movements) {
        this.products = products;
        this.movements = movements;
    }

    @Transactional
    public StockMovement record(StockMovementRequest req) {
        Product p = products.findById(req.productId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        if (p.isService()) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "\"" + p.getName() + "\" is a service — it has no stock");
        }

        BigDecimal change = req.quantity();
        StockMovement.Reason reason;
        String note = req.note() == null ? null : req.note().trim();

        if (req.type() == StockMovementRequest.Type.PURCHASE) {
            if (change.signum() <= 0)
                throw new ResponseStatusException(BAD_REQUEST, "Purchase quantity must be positive");
            reason = StockMovement.Reason.PURCHASE;
            if (req.newCostPrice() != null) p.setCostPrice(req.newCostPrice()); // supplier price drift
        } else {
            if (change.signum() == 0)
                throw new ResponseStatusException(BAD_REQUEST, "Adjustment can't be zero");
            if (note == null || note.isEmpty())
                throw new ResponseStatusException(BAD_REQUEST, "Adjustments need a reason note");
            reason = StockMovement.Reason.ADJUSTMENT;
        }

        BigDecimal newQty = p.getStockQty().add(change);
        if (newQty.signum() < 0) {
            throw new ResponseStatusException(CONFLICT,
                    "Stock can't go negative — \"" + p.getName() + "\" has " + p.getStockQty());
        }
        p.setStockQty(newQty);
        return movements.save(new StockMovement(p, change, reason, note));
    }
}
