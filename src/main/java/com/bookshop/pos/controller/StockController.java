package com.bookshop.pos.controller;

import com.bookshop.pos.dto.StockMovementRequest;
import com.bookshop.pos.dto.StockMovementResponse;
import com.bookshop.pos.repository.StockMovementRepository;
import com.bookshop.pos.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@PreAuthorize("hasRole('ADMIN')") // the whole stock area is admin-only
public class StockController {

    private final StockService stockService;
    private final StockMovementRepository movements;

    public StockController(StockService stockService, StockMovementRepository movements) {
        this.stockService = stockService;
        this.movements = movements;
    }

    // @Transactional: StockMovementResponse.from() resolves the lazy `product`
    // association — needs the session open past the repository call (see ProductController).
    @GetMapping
    @Transactional(readOnly = true)
    public List<StockMovementResponse> recent() {
        return movements.findTop50ByOrderByMovedAtDesc()
                .stream().map(StockMovementResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse record(@Valid @RequestBody StockMovementRequest req) {
        return StockMovementResponse.from(stockService.record(req));
    }
}
