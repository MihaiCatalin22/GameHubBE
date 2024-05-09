package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.PurchaseRepository;
import com.gamehub.backend.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PurchaseServiceImplTest {
    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private User user;
    private Game game;
    private Purchase purchase;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("TestUser");

        game = new Game();
        game.setId(1L);
        game.setTitle("Test Game");
        game.setPrice(19.99);

        purchase = new Purchase();
        purchase.setUser(user);
        purchase.setGame(game);
        purchase.setAmount(19.99);
        purchase.setPurchaseDate(new Date());

        user.setPurchases(List.of(purchase));
    }

    @Test
    void purchaseGame() {
        Game newGame = new Game();
        newGame.setId(2L);
        newGame.setTitle("Another Test Game");
        newGame.setPrice(29.99);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(2L)).thenReturn(Optional.of(newGame));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Purchase result = purchaseService.purchaseGame(1L, 2L);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(newGame, result.getGame());
        assertEquals(29.99, result.getAmount());
        verify(userRepository).findById(1L);
        verify(gameRepository).findById(2L);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void purchaseGameWithNonexistentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            purchaseService.purchaseGame(1L, 1L);
        });

        assertEquals("User not found with id 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(gameRepository, never()).findById(anyLong());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void purchaseGameWithNonexistentGame() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            purchaseService.purchaseGame(1L, 1L);
        });

        assertEquals("Game not found with id 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(gameRepository).findById(1L);
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void purchaseGameAlreadyOwned() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            purchaseService.purchaseGame(1L, 1L);
        });

        assertEquals("Game already owned by the user", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(gameRepository).findById(1L);
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void getPurchases() {
        Date fromDate = new Date(System.currentTimeMillis() - 86400000L);
        when(purchaseRepository.findByUserIdAndPurchaseDateAfter(1L, fromDate)).thenReturn(user.getPurchases());

        List<Purchase> purchases = purchaseService.getPurchases(1L, fromDate, null, null);

        assertNotNull(purchases);
        assertFalse(purchases.isEmpty());
        assertEquals(user.getPurchases().size(), purchases.size());
        verify(purchaseRepository).findByUserIdAndPurchaseDateAfter(1L, fromDate);
    }

    @Test
    void getPurchasesWithMinAmount() {
        Date fromDate = new Date(System.currentTimeMillis() - 86400000L); // 1 day ago
        when(purchaseRepository.findByUserIdAndPurchaseDateAfterAndAmountGreaterThanEqual(1L, fromDate, 10.0))
                .thenReturn(user.getPurchases());

        List<Purchase> purchases = purchaseService.getPurchases(1L, fromDate, 10.0, null);

        assertNotNull(purchases);
        assertFalse(purchases.isEmpty());
        assertEquals(user.getPurchases().size(), purchases.size());
        verify(purchaseRepository).findByUserIdAndPurchaseDateAfterAndAmountGreaterThanEqual(1L, fromDate, 10.0);
    }

    @Test
    void getPurchasesWithMaxAmount() {
        Date fromDate = new Date(System.currentTimeMillis() - 86400000L); // 1 day ago
        when(purchaseRepository.findByUserIdAndPurchaseDateAfterAndAmountLessThan(1L, fromDate, 30.0))
                .thenReturn(user.getPurchases());

        List<Purchase> purchases = purchaseService.getPurchases(1L, fromDate, null, 30.0);

        assertNotNull(purchases);
        assertFalse(purchases.isEmpty());
        assertEquals(user.getPurchases().size(), purchases.size());
        verify(purchaseRepository).findByUserIdAndPurchaseDateAfterAndAmountLessThan(1L, fromDate, 30.0);
    }
}

