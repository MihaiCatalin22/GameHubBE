package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Genre;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private Game game;
    private User user;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1L);
        game.setTitle("Test Game");
        game.setDescription("Test Description");
        game.setGenres(new HashSet<>(Collections.singletonList(Genre.MMO)));
        game.setReleaseDate(new Date());
        game.setDeveloper("Test Developer");
        game.setPrice(29.99);

        user = new User();
        user.setId(1L);
        user.setUsername("Test User");
        Purchase purchase = new Purchase();
        purchase.setGame(game);
        user.setPurchases(Collections.singletonList(purchase));
    }

    @Test
    void createGame() {
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        Game createdGame = gameService.createGame(game);

        assertNotNull(createdGame);
        assertEquals(game.getId(), createdGame.getId());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void createGameWithInvalidData() {
        Game nullTitleGame = new Game();
        nullTitleGame.setTitle(null);

        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(nullTitleGame);
        });
        assertEquals("Game title cannot be empty", exception1.getMessage());

        Game emptyTitleGame = new Game();
        emptyTitleGame.setTitle("");

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(emptyTitleGame);
        });
        assertEquals("Game title cannot be empty", exception2.getMessage());

        Game whitespaceTitleGame = new Game();
        whitespaceTitleGame.setTitle("   ");

        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(whitespaceTitleGame);
        });
        assertEquals("Game title cannot be empty", exception3.getMessage());

        verify(gameRepository, never()).save(any(Game.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "null" })
    void createGameWithInvalidDeveloper(String developer) {
        game.setDeveloper(developer.equals("null") ? null : developer);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(game);
        });
        assertEquals("Developer cannot be empty", exception.getMessage());
    }

    @Test
    void createGameWithNullReleaseDate() {
        game.setReleaseDate(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(game);
        });
        assertEquals("Release date cannot be null", exception.getMessage());
    }

    @Test
    void createGameWithNullPrice() {
        game.setPrice(null);
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        Game createdGame = gameService.createGame(game);

        assertNotNull(createdGame);
        assertEquals(game.getId(), createdGame.getId());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void createGameWithNegativePrice() {
        game.setPrice(-1.0);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(game);
        });
        assertEquals("Price must be positive", exception.getMessage());
    }

    @Test
    void createGameWithZeroPrice() {
        game.setPrice(0.0);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(game);
        });
        assertEquals("Price must be positive", exception.getMessage());
    }

    @Test
    void createGameWithDuplicateTitle() {
        when(gameRepository.existsByTitleAndIdNot(game.getTitle(), null)).thenReturn(true);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(game);
        });
        assertEquals("Game title already exists", exception.getMessage());
    }

    @Test
    void getGameById() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        Optional<Game> foundGame = gameService.getGameById(1L);

        assertTrue(foundGame.isPresent());
        assertEquals(game.getId(), foundGame.get().getId());
        verify(gameRepository).findById(1L);
    }

    @Test
    void getGameByIdWithNonexistentId() {
        Long nonexistentGameId = 999L;
        when(gameRepository.findById(nonexistentGameId)).thenReturn(Optional.empty());

        Optional<Game> result = gameService.getGameById(nonexistentGameId);
        assertFalse(result.isPresent());
        verify(gameRepository).findById(nonexistentGameId);
    }

    @Test
    void getAllGames() {
        when(gameRepository.findAll()).thenReturn(Arrays.asList(game));
        List<Game> games = gameService.getAllGames();

        assertFalse(games.isEmpty());
        assertEquals(1, games.size());
        verify(gameRepository).findAll();
    }

    @Test
    void getAllGamesWhenNoneExist() {
        when(gameRepository.findAll()).thenReturn(Arrays.asList());
        List<Game> games = gameService.getAllGames();
        assertTrue(games.isEmpty());
        verify(gameRepository).findAll();
    }

    @Test
    void getGamesByUserId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<Game> games = gameService.getGamesByUserId(1L);

        assertNotNull(games);
        assertFalse(games.isEmpty());
        assertEquals(1, games.size());
        assertEquals(game.getId(), games.get(0).getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void getGamesByUserIdWithNonexistentUser() {
        Long nonexistentUserId = 999L;
        when(userRepository.findById(nonexistentUserId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            gameService.getGamesByUserId(nonexistentUserId);
        });

        assertEquals("User not found with id 999", exception.getMessage());
        verify(userRepository).findById(nonexistentUserId);
    }

    @Test
    void updateGame() {
        Game updatedGameData = new Game();
        updatedGameData.setTitle("Updated Title");
        updatedGameData.setDescription("Updated Description");
        updatedGameData.setPrice(19.99);
        updatedGameData.setReleaseDate(new Date());
        updatedGameData.setDeveloper("New developer");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game updatedGame = gameService.updateGame(1L, updatedGameData);

        assertNotNull(updatedGame);
        assertEquals("Updated Title", updatedGame.getTitle());
        assertEquals("Updated Description", updatedGame.getDescription());
        assertEquals(19.99, updatedGame.getPrice());
        verify(gameRepository).save(game);
    }

    @Test
    void updateNonexistentGame() {
        Long nonexistentGameId = 999L;
        Game updatedData = new Game();
        updatedData.setTitle("Nonexistent Game");

        lenient().when(gameRepository.findById(nonexistentGameId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            gameService.updateGame(nonexistentGameId, updatedData);
        });
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void deleteGame() {
        doNothing().when(gameRepository).deleteById(1L);

        gameService.deleteGame(1L);

        verify(gameRepository).deleteById(1L);
    }

    @Test
    void deleteNonexistentGame() {
        Long nonexistentGameId = 999L;
        doThrow(new RuntimeException("Game not found.")).when(gameRepository).deleteById(nonexistentGameId);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            gameService.deleteGame(nonexistentGameId);
        });

        assertTrue(exception.getMessage().contains("Game not found"));
        verify(gameRepository).deleteById(nonexistentGameId);
    }
}
