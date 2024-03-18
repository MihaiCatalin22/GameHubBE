package com.Gamehub.backend.business.impl;

import com.Gamehub.backend.domain.Game;
import com.Gamehub.backend.domain.Genre;
import com.Gamehub.backend.persistence.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1L);
        game.setTitle("Test Game");
        game.setDescription("Test Description");
        game.setGenre(Genre.MMO);
        game.setReleaseDate(new Date());
        game.setDeveloper("Test Developer");


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
        Game invalidGame = new Game();
        invalidGame.setTitle(null);

        assertThrows(RuntimeException.class, () -> {
            gameService.createGame(invalidGame);
        });
        verify(gameRepository, never()).save(any(Game.class));
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
    void updateGame() {
        Game updatedGameData = new Game();
        updatedGameData.setTitle("Updated Title");
        updatedGameData.setDescription("Updated Description");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game updatedGame = gameService.updateGame(1L, updatedGameData);

        assertNotNull(updatedGame);
        assertEquals("Updated Title", updatedGame.getTitle());
        assertEquals("Updated Description", updatedGame.getDescription());
        verify(gameRepository).save(game);
    }
    @Test
    void updateNonexistentGame() {
        Long nonexistentGameId = 999L;
        Game updatedData = new Game();

        updatedData.setTitle("Nonexistent Game");

        when(gameRepository.findById(nonexistentGameId)).thenReturn(Optional.empty());

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