package com.example.anagram;

import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.*;

@Component
public class AnagramIndex {
    private final Map<String, List<String>> bySignature;

    public AnagramIndex() {
        bySignature = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("anagrams.txt").getInputStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (word.isEmpty()) continue;
                String sig = signature(word);
                bySignature.computeIfAbsent(sig, k -> new ArrayList<>()).add(word);
            }
        } catch (IOException e) {
            System.err.println("Failed to load anagrams.txt: " + e.getMessage());
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
