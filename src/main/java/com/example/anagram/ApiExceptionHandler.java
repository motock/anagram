package com.example.anagram;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        var body = new java.util.HashMap<String, Object>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        body.put("fieldErrors", fieldErrors.stream()
                .map(fe -> java.util.Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                .collect(Collectors.toList()));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleMalformed(HttpMessageNotReadableException ex) {
        var body = new java.util.HashMap<String, Object>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Malformed JSON request");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
