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
                        new ClassPathResource("words.txt").getInputStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (word.isEmpty()) continue;
                String sig = signature(word);
                bySignature.computeIfAbsent(sig, k -> new ArrayList<>()).add(word);
            }
        } catch (IOException e) {
            // Log and continue with empty map
            System.err.println("Failed to load words.txt: " + e.getMessage());
        }
    }

    public List<String> wordsFor(String signature) {
        List<String> list = bySignature.get(signature);
        if (list == null) return List.of();
        return List.copyOf(list);
    }

    static String signature(String word) {
        char[] chars = word.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }
}
