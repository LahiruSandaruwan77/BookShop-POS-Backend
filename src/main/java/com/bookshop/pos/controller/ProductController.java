package com.bookshop.pos.controller;

import com.bookshop.pos.dto.ProductRequest;
import com.bookshop.pos.dto.ProductResponse;
import com.bookshop.pos.repository.ProductRepository;
import com.bookshop.pos.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository products;
    private final ProductService productService;

    public ProductController(ProductRepository products, ProductService productService) {
        this.products = products;
        this.productService = productService;
    }


    @GetMapping
    @Transactional(readOnly = true)
    public List<ProductResponse> all(@RequestParam(defaultValue = "false") boolean includeInactive) {
        var list = includeInactive ? products.findAll() : products.findByActiveTrue();
        return list.stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public List<ProductResponse> search(@RequestParam String q) {
        return products.findTop10ByNameContainingIgnoreCaseAndActiveTrue(q)
                .stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/barcode/{code}")
    @Transactional(readOnly = true)
    public ProductResponse byBarcode(@PathVariable String code) {
        return products.findByBarcodeAndActiveTrue(code)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No product with barcode " + code));
    }



    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest req) {
        return ProductResponse.from(productService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        return ProductResponse.from(productService.update(id, req));
    }

    
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProductResponse setActive(@PathVariable Long id, @RequestParam boolean value) {
        return ProductResponse.from(productService.setActive(id, value));
    }
}
