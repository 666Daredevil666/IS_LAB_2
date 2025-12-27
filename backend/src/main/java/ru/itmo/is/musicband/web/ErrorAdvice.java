package ru.itmo.is.musicband.web;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class ErrorAdvice {
    private static final Logger log = LoggerFactory.getLogger(ErrorAdvice.class);

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
    public ResponseEntity<?> badRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> unreadable(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> generic(RuntimeException ex) {
        log.warn("Runtime exception: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);
        String msg = ex.getMessage();
        String payload = msg == null ? ex.getClass().getSimpleName() : ex.getClass().getSimpleName() + ": " + msg;
        if ("Not found".equals(msg)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", msg));
        }
        if (ex instanceof IllegalStateException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", payload));
        }
        if (ex instanceof ObjectOptimisticLockingFailureException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", payload));
        }
        if (ex instanceof IncorrectResultSizeDataAccessException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", payload));
        }
        Throwable cause = ex.getCause();
        if (cause instanceof org.springframework.dao.OptimisticLockingFailureException ||
                cause instanceof ObjectOptimisticLockingFailureException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", payload));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", payload));
    }
}
