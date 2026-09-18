package com.example.anagram;

import jakarta.validation.constraints.NotBlank;

public record AnagramRequest(@NotBlank(message = "name must not be blank") String name) {}
