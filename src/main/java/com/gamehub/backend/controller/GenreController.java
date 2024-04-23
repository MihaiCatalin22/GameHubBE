package com.gamehub.backend.controller;

import com.gamehub.backend.domain.Genre;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenreController {
    @GetMapping("/genres")
    public Genre[] getGenres() {
        return Genre.values();
    }
}
