package com.bookshop.pos.controller;

import com.bookshop.pos.dto.DiscountRequest;
import com.bookshop.pos.dto.DiscountResponse;
import com.bookshop.pos.service.DiscountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// the whole discount area is admin-only
@RestController
@RequestMapping("/api/discounts")
@PreAuthorize("hasRole('ADMIN')")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<DiscountResponse> list() {
        return discountService.list().stream().map(DiscountResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public DiscountResponse create(@Valid @RequestBody DiscountRequest req) {
        return DiscountResponse.from(discountService.create(req));
    }

    @PatchMapping("/{id}/active")
    @Transactional
    public DiscountResponse setActive(@PathVariable Long id, @RequestParam boolean value) {
        return DiscountResponse.from(discountService.setActive(id, value));
    }
}
