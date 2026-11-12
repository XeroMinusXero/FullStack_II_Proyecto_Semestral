package com.thequietchapter.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ErroresApi {
    private ResponseEntity<ProblemDetail> error(HttpStatus status, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle("Error en Checkout Service");
        return ResponseEntity.status(status).body(p);
    }

    @ExceptionHandler({ IllegalArgumentException.class, ConstraintViolationException.class })
    ResponseEntity<ProblemDetail> invalido(Exception e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validacion(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).distinct().reduce((a, b) -> a + "; " + b)
                .orElse("Solicitud inválida");
        return error(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    ResponseEntity<ProblemDetail> http(org.springframework.web.server.ResponseStatusException e) {
        HttpStatus s = HttpStatus.resolve(e.getStatusCode().value());
        return error(s == null ? HttpStatus.BAD_REQUEST : s, e.getReason());
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ProblemDetail> falta(NoSuchElementException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({ IllegalStateException.class, OptimisticLockingFailureException.class })
    ResponseEntity<ProblemDetail> conflicto(Exception e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }
}
