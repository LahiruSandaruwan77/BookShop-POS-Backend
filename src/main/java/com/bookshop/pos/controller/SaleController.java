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
        
        return SaleResponse.from(saleService.checkout(request, principal.getName()));
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public SaleResponse get(@PathVariable Long id) {
        return SaleResponse.from(saleService.get(id));
    }
}
