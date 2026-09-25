package com.example.similarproducts.driving.controllers.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.similarproducts.application.exceptions.SimilarProductsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SimilarProductsException.class)
    public ResponseEntity<String> handleSimilarProductsException(
            SimilarProductsException exception) {

        HttpStatus status = exception.getErrorCode() != null
                ? exception.getErrorCode()
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(status)
                .body(exception.getMessage());
    }
}