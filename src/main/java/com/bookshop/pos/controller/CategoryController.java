package com.bookshop.pos.controller;

import com.bookshop.pos.entity.Category;
import com.bookshop.pos.repository.CategoryRepository;
import com.bookshop.pos.repository.ProductRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryRepository categories;
    private final ProductRepository products;

    public CategoryController(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return categories.findAll().stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        categories.findByNameIgnoreCase(name).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        });
        Category c = categories.save(new Category(name));
        return Map.of("id", c.getId(), "name", c.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Category c = categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        if (products.existsByCategoryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Can't delete \"" + c.getName() + "\" — it still has products assigned");
        }
        categories.delete(c);
    }
}
