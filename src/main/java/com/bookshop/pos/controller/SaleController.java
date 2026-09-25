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


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse checkout(@Valid @RequestBody SaleRequest request, Principal principal) {

        return SaleResponse.from(saleService.checkout(request, principal.getName()));
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SaleHeaderResponse> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return saleService.listBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public SaleResponse get(@PathVariable Long id) {
        return SaleResponse.from(saleService.get(id));
    }
}
