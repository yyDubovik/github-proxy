package org.example.githubproxy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(HttpClientErrorException.class)
    ResponseEntity<Object> handleHttpClientError(HttpClientErrorException ex) {
        String message;
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            message = "User not found";
        } else {
            message = ex.getMessage();
        }

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ApiErrorResponse(ex.getStatusCode().value(), message));
    }
}
