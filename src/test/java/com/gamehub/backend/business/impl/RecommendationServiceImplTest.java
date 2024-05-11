package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Genre;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private User user;
    private Game game;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        game = new Game();
        game.setId(1L);
        game.setTitle("Cyberpunk 2077");
        game.setGenres(EnumSet.of(Genre.ACTION, Genre.OPEN_WORLD));

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setGame(game);

        lenient().when(purchaseRepository.findByUserId(1L)).thenReturn(Collections.singletonList(purchase));
        lenient().when(gameRepository.findRecommendations(any(), any())).thenReturn(Collections.singletonList(game));
    }

    @Test
    void getRecommendationsForUser_withValidUser_shouldReturnRecommendations() {
        List<Game> recommendations = recommendationService.getRecommendationsForUser(1L);

        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertEquals(game.getTitle(), recommendations.get(0).getTitle());
        verify(purchaseRepository).findByUserId(1L);
        verify(gameRepository, times(2)).findRecommendations(any(), any());
    }

    @Test
    void getRecommendationsForUser_withNoPurchases_shouldReturnEmptyList() {
        when(purchaseRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        List<Game> recommendations = recommendationService.getRecommendationsForUser(1L);

        assertTrue(recommendations.isEmpty(), "The recommendations list should be empty because there are no purchases.");
        verify(purchaseRepository).findByUserId(1L);
        verify(gameRepository, never()).findRecommendations(any(), any());
    }
}

