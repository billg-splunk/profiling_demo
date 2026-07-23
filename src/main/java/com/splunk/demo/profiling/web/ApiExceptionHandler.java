package com.splunk.demo.profiling.web;

import com.splunk.demo.profiling.scenario.ScenarioNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ScenarioNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ScenarioNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "The request must include a non-null 'enabled' boolean",
                request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleInvalidConfiguration(
            IllegalStateException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path));
    }

    public record ApiError(Instant timestamp, int status, String error, String message, String path) {
    }
}
