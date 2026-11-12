package com.thequietchapter.controller;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ErroresApi {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> estado(ResponseStatusException e) {
        return error(e.getStatusCode(), e.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud invalida: "
                + e.getBindingResult().getFieldErrors().stream().map(f -> f.getField()).distinct().toList());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argumento(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> duplicado(DataIntegrityViolationException e) {
        return error(HttpStatus.CONFLICT, "Conflicto con restricciones de datos");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> concurrencia(ObjectOptimisticLockingFailureException e) {
        return error(HttpStatus.CONFLICT, "Datos modificados simultaneamente: reintentar");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatusCode status, Object mensaje) {
        return ResponseEntity.status(status).body(Map.of("timestamp", OffsetDateTime.now().toString(), "status",
                status.value(), "error", mensaje == null ? "Error" : mensaje));
    }
}
