package com.gamehub.backend.business.impl;

import com.gamehub.backend.business.PurchaseService;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.dto.PurchaseDTO;
import com.gamehub.backend.dto.GamesSalesStatisticsDTO;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.PurchaseRepository;
import com.gamehub.backend.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PurchaseServiceImpl implements PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Autowired
    public PurchaseServiceImpl(PurchaseRepository purchaseRepository, UserRepository userRepository, GameRepository gameRepository) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }
    @Override
    public Purchase purchaseGame(Long userId, Long gameId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id " + gameId));

        if (user.getPurchases().stream().anyMatch(purchase -> purchase.getGame().equals(game))) {
            throw new IllegalStateException("Game already owned by the user");
        }

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setGame(game);
        purchase.setAmount(game.getPrice());
        purchase.setPurchaseDate(new Date());

        List<Purchase> purchases = new ArrayList<>(user.getPurchases());
        purchases.add(purchase);
        user.setPurchases(purchases);

        return purchaseRepository.save(purchase);
    }

    @Override
    public List<PurchaseDTO> getPurchases(Long userId, Date fromDate, Double minAmount, Double maxAmount) {

        List<Purchase> purchases;
        if (minAmount != null && maxAmount != null) {
            purchases = purchaseRepository.findByUserIdAndPurchaseDateAfterAndAmountBetween(userId, fromDate, minAmount, maxAmount);
        } else if (minAmount != null) {
            purchases = purchaseRepository.findByUserIdAndPurchaseDateAfterAndAmountGreaterThanEqual(userId, fromDate, minAmount);
        } else if (maxAmount != null) {
            purchases = purchaseRepository.findByUserIdAndPurchaseDateAfterAndAmountLessThan(userId, fromDate, maxAmount);
        } else {
            purchases = purchaseRepository.findByUserIdAndPurchaseDateAfter(userId, fromDate);
        }


        return purchases.stream()
                .map(purchase -> new PurchaseDTO(
                        purchase.getId(),
                        purchase.getGame().getTitle(),
                        purchase.getAmount(),
                        purchase.getPurchaseDate()
                ))
                .toList();
    }
    @Override
    public boolean checkOwnership(Long userId, Long gameId) {
        return purchaseRepository.existsByUserIdAndGameId(userId, gameId);
    }

    @Override
    public List<GamesSalesStatisticsDTO> getSalesStatistics(String gameTitle, int days) {
        Date startDate = null;
        Date endDate = new Date();

        if (days > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.add(Calendar.DAY_OF_YEAR, -days);
            startDate = cal.getTime();
        }

        List<Object[]> stats = purchaseRepository.findGameSalesStatisticsByTitleAndDateRange(
                gameTitle == null ? "" : gameTitle,
                startDate,
                days == 0 ? null : endDate
        );

        List<GamesSalesStatisticsDTO> result = new ArrayList<>();
        for (Object[] stat : stats) {
            String title = (String) stat[0];
            Long totalUnitsSold = ((Number) stat[1]).longValue();
            Double totalRevenue = stat[2] != null ? (Double) stat[2] : 0.0;
            Double averageRating = stat[3] != null ? (Double) stat[3] : 0.0;

            result.add(new GamesSalesStatisticsDTO(title, totalUnitsSold, totalRevenue, averageRating));
        }

        return result;
    }

}
