package com.example.anagram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class AnagramService {

    private final AnagramIndex index;

    public AnagramService(AnagramIndex index) {
        this.index = index;
    }

    public List<String> findAnagrams(String name) {
        String normalized = normalize(name);
        if (normalized.isEmpty()) {
            return List.of();
        }
        String signature = AnagramIndex.signature(normalized);
        List<String> candidates = new ArrayList<>(index.wordsFor(signature));
        candidates.removeIf(word -> word.equals(normalized));
        candidates.sort(Comparator.naturalOrder());
        return List.copyOf(candidates);
    }

    static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }
}
