package com.gamehub.backend.business;

import com.gamehub.backend.domain.Game;

import java.util.List;
import java.util.Optional;

public interface GameService {
    Game createGame(Game game);
    Optional<Game> getGameById(Long id);
    List<Game> getAllGames();
    Game updateGame(Long id, Game game);
    void deleteGame(Long id);
    List<Game> getGamesByUserId(Long userId);
}
