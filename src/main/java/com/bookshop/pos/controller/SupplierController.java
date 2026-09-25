package com.bookshop.pos.controller;

import com.bookshop.pos.entity.Supplier;
import com.bookshop.pos.repository.ProductRepository;
import com.bookshop.pos.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@Validated
public class SupplierController {

    private final SupplierRepository suppliers;
    private final ProductRepository products;

    public SupplierController(SupplierRepository suppliers, ProductRepository products) {
        this.suppliers = suppliers;
        this.products = products;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return suppliers.findAll().stream()
                .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier name is required");
        suppliers.findByNameIgnoreCase(name).ifPresent(s -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Supplier already exists");
        });
        Supplier s = suppliers.save(new Supplier(name));
        return Map.of("id", s.getId(), "name", s.getName());
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        Supplier s = suppliers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
        products.clearSupplier(id);
        suppliers.delete(s);
    }
}
