package com.gamehub.backend.business;

import com.gamehub.backend.domain.Game;

import java.util.List;

public interface RecommendationService {

    List<Game> getRecommendationsForUser(Long userId);
}
