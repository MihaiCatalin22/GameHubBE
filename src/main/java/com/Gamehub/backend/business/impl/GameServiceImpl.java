package com.Gamehub.backend.business.impl;

import com.Gamehub.backend.domain.Game;
import com.Gamehub.backend.persistence.GameRepository;
import com.Gamehub.backend.business.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame(Game game) {
        if (game.getTitle() == null || game.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Game title cannot be empty");
        }
        return gameRepository.save(game);
    }

    @Override
    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    @Override
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Override
    public Game updateGame(Long id, Game updatedGame) {
        return gameRepository.findById(id)
                .map(game -> {
                    game.setTitle(updatedGame.getTitle());
                    game.setDescription(updatedGame.getDescription());
                    game.setGenre(updatedGame.getGenre());
                    game.setReleaseDate(updatedGame.getReleaseDate());
                    game.setDeveloper(updatedGame.getDeveloper());
                    return gameRepository.save(game);
                })
                .orElseThrow(() -> new RuntimeException("Game with id " + id + " was not found."));
    }

    @Override
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }
}
