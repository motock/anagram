package com.example.anagram;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class AnagramController {
    private final AnagramService service;

    public AnagramController(AnagramService service) {
        this.service = service;
    }

    @PostMapping("/anagrams")
    public AnagramResponse getAnagrams(@Valid @RequestBody AnagramRequest request) {
        List<String> anagrams = service.findAnagrams(request.getName());
        return new AnagramResponse(anagrams);
    }
}
