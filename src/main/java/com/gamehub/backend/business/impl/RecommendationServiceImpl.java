package com.gamehub.backend.business.impl;

import com.gamehub.backend.business.RecommendationService;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Genre;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final PurchaseRepository purchaseRepository;
    private final GameRepository gameRepository;

    @Autowired
    public RecommendationServiceImpl(PurchaseRepository purchaseRepository, GameRepository gameRepository)
    {
        this.purchaseRepository = purchaseRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public List<Game> getRecommendationsForUser(Long userId) {
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        if (purchases.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> ownedGameIds = purchases.stream()
                .map(purchase -> purchase.getGame().getId())
                .collect(Collectors.toSet());

        Set<Genre> preferredGenres = purchases.stream()
                .flatMap(purchase -> purchase.getGame().getGenres().stream())
                .collect(Collectors.toSet());


        List<Game> recommendations = new ArrayList<>();
        for (Genre genre : preferredGenres) {
            recommendations.addAll(gameRepository.findRecommendations(genre, ownedGameIds));
        }
        return recommendations;
    }
}
