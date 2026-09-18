package com.example.anagram;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<FieldViolation> fieldErrors) {

    public record FieldViolation(String field, String message) {
    }
}
