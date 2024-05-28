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

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Purchase p WHERE p.user.id = :userId AND p.game.id = :gameId")
    boolean existsByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);

    @Query("SELECT p.game.title, COUNT(p), SUM(p.amount) " +
            "FROM Purchase p " +
            "WHERE (:gameTitle IS NULL OR p.game.title LIKE %:gameTitle%) " +
            "AND (:startDate IS NULL OR p.purchaseDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.purchaseDate <= :endDate) " +
            "GROUP BY p.game.title")
    List<Object[]> findGameSalesStatisticsByTitleAndDateRange(
            @Param("gameTitle") String gameTitle,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
}

