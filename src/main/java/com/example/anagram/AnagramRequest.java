package com.example.anagram;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AnagramRequest {
    @NotBlank(message = "name must not be blank")
    @Size(max = 64, message = "name must be at most 64 characters")
    @Pattern(regexp = "(?s)\\s*|.*[a-zA-Z].*", message = "name must contain at least one letter")
    private String name;

    public AnagramRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
