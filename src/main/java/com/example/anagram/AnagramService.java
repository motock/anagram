package com.example.anagram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

/**
 * Service that provides anagram lookup.
 *
 * <p>The service normalizes input by lower-casing (with {@link Locale#ROOT})
 * and stripping all non-{@code [a-z]} characters. It then looks up the
 * signature in the {@link AnagramIndex} and returns an immutable list of all
 * anagrams except the query itself, sorted alphabetically. The implementation
 * copies the index's list before filtering so that reading never mutates the
 * index, and uses {@link List#copyOf} to guarantee that callers cannot mutate
 * the returned list either.
 */
@Service
public class AnagramService {

    private final AnagramIndex index;

    public AnagramService(AnagramIndex index) {
        this.index = index;
    }

    /**
     * Returns the other members of the query's signature group, sorted
     * alphabetically, excluding the normalized query itself.
     *
     * @param name the raw query; {@code null}, empty or all-non-letter input
     *             yields an empty list
     * @return an immutable, alphabetically sorted list of anagrams
     */
    public List<String> findAnagrams(String name) {
        String normalized = normalize(name);
        if (normalized.isEmpty()) {
            return List.of();
        }
        String signature = AnagramIndex.signature(normalized);
        List<String> candidates = new ArrayList<>(index.wordsFor(signature)); // COPY, never mutate the index's list
        candidates.removeIf(word -> word.equals(normalized));                 // compare to NORMALIZED, not `name`
        candidates.sort(Comparator.naturalOrder());
        return List.copyOf(candidates);
    }

    /**
     * Lower-cases with {@link Locale#ROOT} and strips every character that is
     * not {@code a}-{@code z}. {@code null} becomes the empty string.
     */
    static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }
}