package com.gamehub.backend.business.impl;

import com.gamehub.backend.business.PurchaseService;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.dto.PurchaseDTO;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.PurchaseRepository;
import com.gamehub.backend.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        System.out.printf("Filtering Purchases: userId=%d, fromDate=%s, minAmount=%s, maxAmount=%s%n", userId, fromDate, minAmount, maxAmount);

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

        System.out.printf("Found %d purchases.%n", purchases.size());

        return purchases.stream()
                .map(purchase -> new PurchaseDTO(
                        purchase.getId(),
                        purchase.getGame().getTitle(),
                        purchase.getAmount(),
                        purchase.getPurchaseDate()
                ))
                .toList();
    }
}
