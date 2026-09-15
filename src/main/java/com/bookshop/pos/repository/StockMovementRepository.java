package com.bookshop.pos.repository;

import com.bookshop.pos.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findTop50ByOrderByMovedAtDesc();
    List<StockMovement> findByProductIdOrderByMovedAtDesc(Long productId);
}
