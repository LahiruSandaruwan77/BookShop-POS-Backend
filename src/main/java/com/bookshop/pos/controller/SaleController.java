package com.bookshop.pos.controller;

import com.bookshop.pos.dto.SaleRequest;
import com.bookshop.pos.dto.SaleResponse;
import com.bookshop.pos.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse checkout(@Valid @RequestBody SaleRequest request, Principal principal) {
        // Principal = the logged-in user, provided by Spring Security.
        // Until security is wired up, DevSecurityConfig makes this work with a default user.
        return SaleResponse.from(saleService.checkout(request, principal.getName()));
    }

    // @Transactional: joins SaleService.get()'s read-only transaction so the session
    // stays open while SaleResponse.from() resolves the lazy `user` and each item's
    // lazy `product` — get() itself only touches the items collection, not those.
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public SaleResponse get(@PathVariable Long id) {
        return SaleResponse.from(saleService.get(id));
    }
}
