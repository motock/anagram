package com.example.anagram;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnagramController {
    private final AnagramService service;

    public AnagramController(AnagramService service) {
        this.service = service;
    }

    @PostMapping("/anagrams")
    public AnagramResponse anagrams(@Valid @RequestBody AnagramRequest request) {
        return new AnagramResponse(service.findAnagrams(request.name()));
    }
}
