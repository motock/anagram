package com.example.anagram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Spec tests for {@link AnagramService}.
 *
 * <p>The contract under test:
 * <ul>
 *   <li>{@code findAnagrams} returns the other members of the query's signature
 *       group, alphabetically sorted, excluding the query itself.</li>
 *   <li>Input is normalized (lower-cased, non {@code [a-z]} characters dropped)
 *       before lookup and before self-exclusion.</li>
 *   <li>{@code null}, empty and blank input yield an empty list.</li>
 *   <li>Reading the index must never mutate it: repeated calls, and calls for a
 *       different member of the same group, must keep returning the full group.</li>
 * </ul>
 */
class AnagramServiceTest {

    /** The signature group of "listen" as stored in words.txt (insertion order). */
    private static final List<String> EILNST_GROUP =
            List.of("enlist", "inlets", "listen", "silent", "tinsel");

    /** The expected answer for "listen": the group minus the query itself. */
    private static final List<String> LISTEN_ANAGRAMS =
            List.of("enlist", "inlets", "silent", "tinsel");

    private static AnagramService newService() {
        return new AnagramService(new AnagramIndex());
    }

    @Test
    void findAnagramsReturnsTheOtherMembersOfTheSignatureGroup() {
        AnagramService service = newService();

        List<String> result = service.findAnagrams("listen");

        assertEquals(4, result.size(), "eilnst has 5 members, one of which is the query");
        assertEquals(LISTEN_ANAGRAMS, result);
    }

    @Test
    void findAnagramsExcludesTheQueryItself() {
        AnagramService service = newService();

        List<String> result = service.findAnagrams("listen");

        assertEquals(4, result.size());
        assertFalse(result.contains("listen"), "the query must not be its own anagram");
    }

    @Test
    void findAnagramsNormalizesCasePunctuationAndWhitespace() {
        AnagramService service = newService();

        assertEquals(LISTEN_ANAGRAMS, service.findAnagrams("Listen!"));
        assertEquals(LISTEN_ANAGRAMS, service.findAnagrams("  listen  "));
        assertEquals(LISTEN_ANAGRAMS, service.findAnagrams("L-I-S-T-E-N!"));
    }

    @Test
    void findAnagramsReturnsEmptyListForNullEmptyAndBlankInput() {
        AnagramService service = newService();

        assertEquals(List.of(), service.findAnagrams(null));
        assertEquals(List.of(), service.findAnagrams(""));
        assertEquals(List.of(), service.findAnagrams("   "));
    }

    @Test
    void findAnagramsReturnsEmptyListWhenThereIsNoMatch() {
        AnagramService service = newService();

        assertEquals(List.of(), service.findAnagrams("zzzz"));
    }

    @Test
    void normalizeLowercasesAndStripsNonLetters() {
        assertEquals("listen", AnagramService.normalize("Listen!"));
        assertEquals("listen", AnagramService.normalize("  listen  "));
        assertEquals("", AnagramService.normalize(null));
        assertEquals("", AnagramService.normalize(""));
        assertEquals("", AnagramService.normalize("  "));
        // Non-ASCII letters are dropped rather than rejected; acceptable because
        // words.txt is pure [a-z]+.
        assertEquals("caf", AnagramService.normalize("café"));
    }

    @Test
    void repeatedCallsDoNotMutateTheIndex() {
        AnagramIndex index = new AnagramIndex();
        AnagramService service = new AnagramService(index);

        // Call 1: the query is removed from the returned copy.
        assertEquals(LISTEN_ANAGRAMS, service.findAnagrams("listen"));

        // Call 2 on the same index: the answer must be identical.
        assertEquals(LISTEN_ANAGRAMS, service.findAnagrams("listen"));

        // The index itself must still hold the full 5-member group. If the
        // implementation aliased the index's list instead of copying it, the
        // first call would have shrunk this group to 4 and "listen" would be
        // gone forever.
        List<String> stored = index.wordsFor("eilnst");
        assertEquals(5, stored.size(), "findAnagrams must not mutate the index's stored group");
        assertTrue(stored.containsAll(EILNST_GROUP), "the stored group lost members: " + stored);
        assertEquals(EILNST_GROUP, stored);

        // A different member of the same group must still see all four others.
        assertEquals(
                List.of("enlist", "inlets", "listen", "tinsel"),
                service.findAnagrams("silent"));
    }
}
