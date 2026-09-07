package com.example.spring_test_meteo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //Error 404: City not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Resource not available");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // Errore 504 / 502: Problemi del server esterno o Timeout
    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalServiceException(ExternalServiceException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
        problem.setTitle("External dependence error");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // Errore 400: Validazione del DTO fallita (es. stringa vuota)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleArgumentNotValidException(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("violation of validation constraints");

        //Mappiamo i singoli errori sui campi
        Map<String, String> error_details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((org.springframework.validation.FieldError error) -> {
            error_details.put(error.getField(), error.getDefaultMessage());
        });
        problem.setProperty("validation_errors", error_details);
        problem.setProperty("timestamp", Instant.now());
        return problem;

    }
}
