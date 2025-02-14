package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.business.GameService;
import com.gamehub.backend.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository, UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Game createGame(Game game) {
        validateGame(game, null);
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
    public List<Game> getGamesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        return user.getPurchases().stream()
                .map(Purchase::getGame)
                .toList();
    }

    @Override
    public Game updateGame(Long id, Game updatedGame) {
        validateGame(updatedGame, id);
        return gameRepository.findById(id)
                .map(game -> {
                    game.setTitle(updatedGame.getTitle());
                    game.setDescription(updatedGame.getDescription());
                    game.setGenres(updatedGame.getGenres());
                    game.setReleaseDate(updatedGame.getReleaseDate());
                    game.setDeveloper(updatedGame.getDeveloper());
                    game.setPrice(updatedGame.getPrice());
                    return gameRepository.save(game);
                })
                .orElseThrow(() -> new RuntimeException("Game with id " + id + " was not found."));
    }

    @Override
    public void deleteGame(Long id) {
        Game game = gameRepository.findById(id).orElseThrow(() -> new RuntimeException("Game not found"));
        gameRepository.delete(game);
    }

    private void validateGame(Game game, Long gameId) {
        if (!StringUtils.hasText(game.getTitle())) {
            throw new IllegalArgumentException("Game title cannot be empty");
        }
        if (gameRepository.existsByTitleAndIdNot(game.getTitle(), gameId)) {
            throw new IllegalArgumentException("Game title already exists");
        }
        if (game.getReleaseDate() == null) {
            throw new IllegalArgumentException("Release date cannot be null");
        }
        if (!StringUtils.hasText(game.getDeveloper())) {
            throw new IllegalArgumentException("Developer cannot be empty");
        }
        if (game.getPrice() != null && game.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}
