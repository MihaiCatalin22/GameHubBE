package com.gamehub.backend.persistence;

import com.gamehub.backend.domain.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserIdAndPurchaseDateAfter(Long userId, Date fromDate);

    List<Purchase> findByUserIdAndPurchaseDateAfterAndAmountGreaterThanEqual(Long userId, Date fromDate, Double minAmount);

    List<Purchase> findByUserIdAndPurchaseDateAfterAndAmountLessThan(Long userId, Date fromDate, Double maxAmount);

    List<Purchase> findByUserIdAndPurchaseDateAfterAndAmountBetween(Long userId, Date fromDate, Double minAmount, Double maxAmount);
    List<Purchase> findByUserId(Long userId);
    @Query("SELECT p FROM Purchase p WHERE p.user.id = :userId AND p.purchaseDate > :fromDate AND p.amount >= :minAmount AND p.amount < :maxAmount")
    List<Purchase> findPurchasesWithinRange(
            @Param("userId") Long userId,
            @Param("fromDate") Date fromDate,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount
    );
}
