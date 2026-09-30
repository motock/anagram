package com.example.anagram;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Structural check that {@code CLAUDE.md} documents the widened error-body
 * contract: {@code GlobalExceptionHandler} + {@code ErrorResponse} own the body
 * for 400, 404 and 405.
 *
 * <p>Membership based on purpose: the bullet that names
 * {@code GlobalExceptionHandler} must mention all three status codes and must
 * still carry the documented field list. The exact prose is not pinned, so the
 * sentence can be reworded freely.</p>
 */
class DocumentationContractTest {

    private static final String FIELD_LIST = "{timestamp,status,error,message,fieldErrors[]}";

    @Test
    void claudeMdDocumentsErrorBodyFor400And404And405() throws Exception {
        Path claudeMd = projectRoot().resolve("CLAUDE.md");
        assertTrue(Files.isRegularFile(claudeMd), "CLAUDE.md must exist at the repository root");

        List<String> lines = Files.readAllLines(claudeMd);
        String bullet = lines.stream()
                .filter(line -> line.contains("GlobalExceptionHandler"))
                .findFirst()
                .orElse(null);

        assertNotNull(bullet, "CLAUDE.md must document GlobalExceptionHandler owning the error body");
        assertTrue(bullet.contains("ErrorResponse"),
                "the GlobalExceptionHandler bullet must still name ErrorResponse, was: " + bullet);
        assertTrue(bullet.contains("400"), "the bullet must cover 400, was: " + bullet);
        assertTrue(bullet.contains("404"), "the bullet must cover 404, was: " + bullet);
        assertTrue(bullet.contains("405"), "the bullet must cover 405, was: " + bullet);
        assertTrue(bullet.contains(FIELD_LIST),
                "the documented field list must be unchanged, was: " + bullet);
    }

    private static Path projectRoot() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null && !Files.isRegularFile(dir.resolve("pom.xml"))) {
            dir = dir.getParent();
        }
        assertNotNull(dir, "could not locate the project root (a directory containing pom.xml)");
        return dir;
    }
}
