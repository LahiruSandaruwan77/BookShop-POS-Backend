package com.bookshop.pos.controller;

import com.bookshop.pos.entity.Category;
import com.bookshop.pos.repository.CategoryRepository;
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

    public CategoryController(CategoryRepository categories) {
        this.categories = categories;
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
}
