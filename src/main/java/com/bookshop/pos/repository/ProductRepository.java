package com.bookshop.pos.repository;

import com.bookshop.pos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findByBarcodeAndActiveTrue(String barcode);


    List<Product> findTop10ByNameContainingIgnoreCaseAndActiveTrue(String name);

    List<Product> findByActiveTrue();


    List<Product> findByActiveTrueAndServiceFalseAndStockQtyLessThanEqual(java.math.BigDecimal level);


    boolean existsByCategoryId(Long categoryId);


    List<Product> findByActiveTrueAndSupplierId(Long supplierId);
    List<Product> findByActiveTrueAndCategoryIdAndSupplierId(Long categoryId, Long supplierId);

    @Modifying
    @Query("update Product p set p.supplier = null where p.supplier.id = :supplierId")
    void clearSupplier(@Param("supplierId") Long supplierId);
}
