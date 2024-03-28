package com.Gamehub.backend.controller;

import com.Gamehub.backend.domain.Game;
import com.Gamehub.backend.business.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/games")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody Game game) {
        Game createdGame = gameService.createGame(game);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGame);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGameById(@PathVariable Long id) {
        return gameService.getGameById(id)
                .map(game -> ResponseEntity.ok().body(game))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable Long id, @RequestBody Game game) {
        try {
            Game updatedGame = gameService.updateGame(id, game);
            return new ResponseEntity<>(updatedGame, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        try {
            gameService.deleteGame(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
