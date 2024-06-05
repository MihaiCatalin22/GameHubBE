package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.dto.GamesSalesStatisticsDTO;
import com.gamehub.backend.dto.PurchaseDTO;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.PurchaseRepository;
import com.gamehub.backend.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PurchaseServiceImplTest {

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

        List<PurchaseDTO> purchases = purchaseService.getPurchases(1L, fromDate, null, null);

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

        List<PurchaseDTO> purchases = purchaseService.getPurchases(1L, fromDate, 10.0, null);

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

        List<PurchaseDTO> purchases = purchaseService.getPurchases(1L, fromDate, null, 30.0);

        assertNotNull(purchases);
        assertFalse(purchases.isEmpty());
        assertEquals(user.getPurchases().size(), purchases.size());
        verify(purchaseRepository).findByUserIdAndPurchaseDateAfterAndAmountLessThan(1L, fromDate, 30.0);
    }

    @Test
    void checkOwnership() {
        when(purchaseRepository.existsByUserIdAndGameId(1L, 1L)).thenReturn(true);

        boolean ownsGame = purchaseService.checkOwnership(1L, 1L);

        assertTrue(ownsGame);
        verify(purchaseRepository).existsByUserIdAndGameId(1L, 1L);
    }

    @Test
    void checkOwnershipNotOwned() {
        when(purchaseRepository.existsByUserIdAndGameId(1L, 1L)).thenReturn(false);

        boolean ownsGame = purchaseService.checkOwnership(1L, 1L);

        assertFalse(ownsGame);
        verify(purchaseRepository).existsByUserIdAndGameId(1L, 1L);
    }

    @Test
    void getSalesStatisticsAllTime() {
        List<Object[]> mockStats = Collections.singletonList(new Object[]{"Test Game", 3L, 59.97, 4.5});
        when(purchaseRepository.findGameSalesStatisticsByTitleAndDateRange("", null, null)).thenReturn(mockStats);

        List<GamesSalesStatisticsDTO> stats = purchaseService.getSalesStatistics("", 0);

        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertEquals(1, stats.size());
        assertEquals("Test Game", stats.get(0).getGameTitle());
        assertEquals(3L, stats.get(0).getTotalUnitsSold());
        assertEquals(59.97, stats.get(0).getTotalRevenue());
        assertEquals(4.5, stats.get(0).getAverageRating());
        verify(purchaseRepository).findGameSalesStatisticsByTitleAndDateRange("", null, null);
    }

    @Test
    void getSalesStatisticsAllTime_NullRevenueAndRating() {
        List<Object[]> mockStats = Collections.singletonList(new Object[]{"Test Game", 3L, null, null});
        when(purchaseRepository.findGameSalesStatisticsByTitleAndDateRange("", null, null)).thenReturn(mockStats);

        List<GamesSalesStatisticsDTO> stats = purchaseService.getSalesStatistics("", 0);

        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertEquals(1, stats.size());
        assertEquals("Test Game", stats.get(0).getGameTitle());
        assertEquals(3L, stats.get(0).getTotalUnitsSold());
        assertEquals(0.0, stats.get(0).getTotalRevenue());
        assertEquals(0.0, stats.get(0).getAverageRating());
        verify(purchaseRepository).findGameSalesStatisticsByTitleAndDateRange("", null, null);
    }

    @Test
    void getSalesStatisticsNoTitle() {
        List<Object[]> mockStats = Collections.singletonList(new Object[]{"Test Game", 5L, 99.95, 4.7});
        when(purchaseRepository.findGameSalesStatisticsByTitleAndDateRange(anyString(), any(Date.class), any(Date.class)))
                .thenReturn(mockStats);

        int days = 7;
        Date endDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.add(Calendar.DAY_OF_YEAR, -days);
        Date startDate = cal.getTime();

        List<GamesSalesStatisticsDTO> result = purchaseService.getSalesStatistics(null, days);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Game", result.get(0).getGameTitle());
        assertEquals(5L, result.get(0).getTotalUnitsSold());
        assertEquals(99.95, result.get(0).getTotalRevenue());
        assertEquals(4.7, result.get(0).getAverageRating());

        ArgumentCaptor<Date> startDateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endDateCaptor = ArgumentCaptor.forClass(Date.class);

        verify(purchaseRepository).findGameSalesStatisticsByTitleAndDateRange(eq(""), startDateCaptor.capture(), endDateCaptor.capture());

        assertDatesAreEqualIgnoringMillis(startDate, startDateCaptor.getValue());
        assertDatesAreEqualIgnoringMillis(endDate, endDateCaptor.getValue());
    }

    @Test
    void getSalesStatisticsZeroDays() {
        List<Object[]> mockStats = Collections.singletonList(new Object[]{"Test Game", 5L, 99.95, 4.0});
        when(purchaseRepository.findGameSalesStatisticsByTitleAndDateRange("Test Game", null, null))
                .thenReturn(mockStats);

        List<GamesSalesStatisticsDTO> result = purchaseService.getSalesStatistics("Test Game", 0);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Game", result.get(0).getGameTitle());
        assertEquals(5L, result.get(0).getTotalUnitsSold());
        assertEquals(99.95, result.get(0).getTotalRevenue());
        assertEquals(4.0, result.get(0).getAverageRating());
        verify(purchaseRepository).findGameSalesStatisticsByTitleAndDateRange("Test Game", null, null);
    }

    @Test
    void getSalesStatisticsZeroDays_NullRevenueAndRating() {
        List<Object[]> mockStats = Collections.singletonList(new Object[]{"Test Game", 5L, null, null});
        when(purchaseRepository.findGameSalesStatisticsByTitleAndDateRange("Test Game", null, null))
                .thenReturn(mockStats);

        List<GamesSalesStatisticsDTO> result = purchaseService.getSalesStatistics("Test Game", 0);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Game", result.get(0).getGameTitle());
        assertEquals(5L, result.get(0).getTotalUnitsSold());
        assertEquals(0.0, result.get(0).getTotalRevenue());
        assertEquals(0.0, result.get(0).getAverageRating());
        verify(purchaseRepository).findGameSalesStatisticsByTitleAndDateRange("Test Game", null, null);
    }

    @Test
    void getSalesStatisticsNoResults() {
        when(purchaseRepository.findGameSalesStatisticsByTitleAndDateRange("Nonexistent Game", null, null))
                .thenReturn(Collections.emptyList());

        List<GamesSalesStatisticsDTO> result = purchaseService.getSalesStatistics("Nonexistent Game", 0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(purchaseRepository).findGameSalesStatisticsByTitleAndDateRange("Nonexistent Game", null, null);
    }

    @Test
    void getSalesStatisticsWithCalendar() {
        List<Object[]> mockStats = Collections.singletonList(new Object[]{"Test Game", 3L, 59.97, 3.8});
        when(purchaseRepository.findGameSalesStatisticsByTitleAndDateRange(anyString(), any(Date.class), any(Date.class)))
                .thenReturn(mockStats);

        String gameTitle = "Test Game";
        int days = 5;
        Date endDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.add(Calendar.DAY_OF_YEAR, -days);
        Date startDate = cal.getTime();

        List<GamesSalesStatisticsDTO> result = purchaseService.getSalesStatistics(gameTitle, days);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Game", result.get(0).getGameTitle());
        assertEquals(3L, result.get(0).getTotalUnitsSold());
        assertEquals(59.97, result.get(0).getTotalRevenue());
        assertEquals(3.8, result.get(0).getAverageRating());

        ArgumentCaptor<Date> startDateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endDateCaptor = ArgumentCaptor.forClass(Date.class);

        verify(purchaseRepository).findGameSalesStatisticsByTitleAndDateRange(eq(gameTitle), startDateCaptor.capture(), endDateCaptor.capture());

        assertDatesAreEqualIgnoringMillis(startDate, startDateCaptor.getValue());
        assertDatesAreEqualIgnoringMillis(endDate, endDateCaptor.getValue());
    }

    private void assertDatesAreEqualIgnoringMillis(Date expected, Date actual) {
        assertEquals(expected.getTime() / 1000, actual.getTime() / 1000, "Dates are not equal ignoring milliseconds");
    }
}

