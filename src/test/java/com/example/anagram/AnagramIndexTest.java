package com.example.anagram;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

class AnagramIndexTest {

    @Test
    void testSignature() {
        assertEquals("eilnst", AnagramIndex.signature("listen"));
    }

    @Test
    void testWordsForSignature() {
        AnagramIndex index = new AnagramIndex();
        String sig = AnagramIndex.signature("listen");
        var words = index.wordsFor(sig);
        assertNotNull(words);
        assertEquals(5, words.size());
        assertTrue(words.contains("listen"));
        assertTrue(words.contains("silent"));
        assertTrue(words.contains("enlist"));
        assertTrue(words.contains("tinsel"));
        assertTrue(words.contains("inlets"));
    }

    @Test
    void testWordsForMissingSignature() {
        AnagramIndex index = new AnagramIndex();
        var words = index.wordsFor("zzzz");
        assertNotNull(words);
        assertTrue(words.isEmpty());
    }

    @Test
    void missingWordListFailsFast() {
        UncheckedIOException thrown = assertThrows(
                UncheckedIOException.class,
                () -> new AnagramIndex("no-such-word-list.txt"));

        assertTrue(thrown.getMessage().contains("no-such-word-list.txt"),
                "the failure must name the resource that could not be read, got: " + thrown.getMessage());
        assertTrue(thrown.getCause() instanceof FileNotFoundException,
                "the underlying cause must be preserved, got: " + thrown.getCause());
    }

    @Test
    void bundledWordListIsNotAShrunkenFixture() throws Exception {
        try (var in = new ClassPathResource("words.txt").getInputStream()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            long count = text.lines()
                    .map(String::trim)
                    .filter(line -> line.matches("[a-z]+"))
                    .count();
            assertTrue(count >= 100,
                    "words.txt looks like a shrunken fixture: only " + count + " words");
        }
    }
}
