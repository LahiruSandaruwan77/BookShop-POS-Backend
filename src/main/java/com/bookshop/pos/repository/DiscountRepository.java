package com.bookshop.pos.repository;

import com.bookshop.pos.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {


    List<Discount> findByProductIdAndActiveTrue(Long productId);

    @Query("""
            select d from Discount d
            where d.product.id = :productId and d.active = true
              and d.startDate <= :date and d.endDate > :date
            """)
    Optional<Discount> findActiveForProductOn(@Param("productId") Long productId, @Param("date") LocalDate date);

    List<Discount> findAllByOrderByStartDateDesc();
}
