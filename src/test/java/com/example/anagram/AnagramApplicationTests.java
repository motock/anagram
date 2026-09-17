package com.example.anagram;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the full Spring application context. This is the TDD anchor for the
 * story: it fails until {@code AnagramApplication} exists, is annotated with
 * {@code @SpringBootApplication} and the required starters are on the classpath.
 */
@SpringBootTest
class AnagramApplicationTests {

    @Test
    void contextLoads() {
    }
}
