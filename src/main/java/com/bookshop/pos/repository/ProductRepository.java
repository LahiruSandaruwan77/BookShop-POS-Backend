package com.bookshop.pos.repository;

import com.bookshop.pos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // The billing screen's scan path: exact barcode match.
    Optional<Product> findByBarcodeAndActiveTrue(String barcode);

    // The billing screen's search path.
    List<Product> findTop10ByNameContainingIgnoreCaseAndActiveTrue(String name);

    List<Product> findByActiveTrue();

    // Low-stock report: physical items at/below their own reorder level.
    List<Product> findByActiveTrueAndServiceFalseAndStockQtyLessThanEqual(java.math.BigDecimal level);

    // Category delete guard: checks ALL products (active and inactive — soft-deleted
    // products still reference their category), not just the active ones.
    boolean existsByCategoryId(Long categoryId);
}
