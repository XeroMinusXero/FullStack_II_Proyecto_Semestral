package com.thequietchapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class ErroresApi {
    private record ErrorResponse(LocalDateTime fecha, int status, String error) {
    }

    private ResponseEntity<ErrorResponse> err(HttpStatus estado, String detalle) {
        return ResponseEntity.status(estado).body(new ErrorResponse(LocalDateTime.now(), estado.value(), detalle));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).findFirst().orElse("Solicitud inválida");
        return err(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler({ IllegalArgumentException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class })
    public ResponseEntity<ErrorResponse> invalido(Exception e) {
        return err(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(Exception e) {
        return err(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> estadoInvalido(IllegalStateException e) {
        return err(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({ DataIntegrityViolationException.class,
            org.springframework.orm.ObjectOptimisticLockingFailureException.class })
    public ResponseEntity<ErrorResponse> conflicto(Exception e) {
        return err(HttpStatus.CONFLICT, "Datos en conflicto: vuelve a consultar el pedido");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> estado(ResponseStatusException e) {
        HttpStatus s = HttpStatus.resolve(e.getStatusCode().value());
        return err(s == null ? HttpStatus.BAD_REQUEST : s, e.getReason());
    }
}
