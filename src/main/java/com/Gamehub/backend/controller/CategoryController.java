package com.Gamehub.backend.controller;

import com.Gamehub.backend.domain.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @GetMapping
    public List<Category> getAllCategories() {
        return Arrays.asList(Category.values());
    }
}
