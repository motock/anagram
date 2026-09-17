package com.example.anagram;

import org.junit.jupiter.api.Test;
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
}
