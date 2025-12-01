package ru.itmo.is.musicband.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class ErrorAdvice {
  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
  public ResponseEntity<?> badRequest(Exception ex){
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", ex.getMessage()));
  }
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<?> generic(RuntimeException ex){
    String msg = ex.getMessage();
    if("Not found".equals(msg)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", msg));
    if(msg!=null && msg.contains("delete")) return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", msg));
    return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", msg));
  }
}
