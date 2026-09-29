package com.example.anagram;

import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

@Component
public class AnagramIndex {
    private static final String DEFAULT_WORDS = "words.txt";

    private final Map<String, List<String>> bySignature;

    public AnagramIndex() {
        this(DEFAULT_WORDS);
    }

    AnagramIndex(String resourceName) {
        bySignature = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(resourceName).getInputStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (word.isEmpty()) continue;
                String sig = signature(word);
                bySignature.computeIfAbsent(sig, k -> new ArrayList<>()).add(word);
            }
        } catch (IOException e) {
            // The word list is required data: serving an empty index would
            // answer every request with a misleadingly empty result set.
            throw new UncheckedIOException("Failed to load " + resourceName, e);
        }
        // Sort each bucket alphabetically
        bySignature.values().forEach(list -> list.sort(Comparator.naturalOrder()));
    }

    public List<String> wordsFor(String normalized) {
        String sig = signature(normalized);
        List<String> list = bySignature.get(sig);
        if (list == null) return List.of();
        return List.copyOf(list);
    }

    static String signature(String word) {
        char[] chars = word.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }
}
