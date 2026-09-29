package com.example.similarproducts.driving.controllers.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.similarproducts.application.exceptions.ErrorCode;
import com.example.similarproducts.application.exceptions.SimilarProductsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(SimilarProductsException.class)
	public ResponseEntity<String> handleSimilarProductsException(SimilarProductsException exception) {

		HttpStatus status = switch (exception.getErrorCode()) {
		case ErrorCode.NOT_FOUND -> HttpStatus.NOT_FOUND;
		case ErrorCode.GATEWAY_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
		case ErrorCode.BAD_GATEWAY -> HttpStatus.BAD_GATEWAY;
		};

		return ResponseEntity.status(status).body(exception.getMessage());
	}
}