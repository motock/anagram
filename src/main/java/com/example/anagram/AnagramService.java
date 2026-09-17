package com.example.anagram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
/**
 * Service that provides anagram lookup.
 *
 * <p>The service normalizes input by lower‑casing and stripping all
 * non‑{@code [a-z]} characters.  It then looks up the signature in the
 * {@link AnagramIndex} and returns an immutable list of all anagrams
 * except the query itself.  The returned list is sorted
 * alphabetically.  The implementation uses {@link List#copyOf} to
 * guarantee that callers cannot mutate the internal index state.
 */
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
