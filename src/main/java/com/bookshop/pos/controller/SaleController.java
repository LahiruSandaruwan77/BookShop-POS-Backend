package com.bookshop.pos.controller;

import com.bookshop.pos.dto.SaleHeaderResponse;
import com.bookshop.pos.dto.SaleRequest;
import com.bookshop.pos.dto.SaleResponse;
import com.bookshop.pos.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    // No @PreAuthorize: any logged-in user (ADMIN or CASHIER) can ring up a sale.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse checkout(@Valid @RequestBody SaleRequest request, Principal principal) {
        // Principal = the logged-in user, provided by Spring Security.
        // Until security is wired up, DevSecurityConfig makes this work with a default user.
        return SaleResponse.from(saleService.checkout(request, principal.getName()));
    }

    // Sales history list — admin only, so a cashier can't browse past bills (or
    // anyone else's) just by hitting the URL.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SaleHeaderResponse> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return saleService.listBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    // Admin only, same reasoning as list() above — this used to have no
    // @PreAuthorize at all, which meant a cashier could read any bill by id.
    // @Transactional: joins SaleService.get()'s read-only transaction so the session
    // stays open while SaleResponse.from() resolves the lazy `user` and each item's
    // lazy `product` — get() itself only touches the items collection, not those.
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public SaleResponse get(@PathVariable Long id) {
        return SaleResponse.from(saleService.get(id));
    }
}
