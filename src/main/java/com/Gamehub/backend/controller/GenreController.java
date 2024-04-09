package com.Gamehub.backend.controller;

import com.Gamehub.backend.domain.Genre;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenreController {
    @GetMapping("/genres")
    public Genre[] getGenres() {
        return Genre.values();
    }
}
