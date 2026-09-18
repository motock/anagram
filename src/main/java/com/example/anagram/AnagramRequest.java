package com.example.anagram;

import jakarta.validation.constraints.NotBlank;

public class AnagramRequest {
    @NotBlank(message = "name must not be blank")
    private String name;

    public AnagramRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
