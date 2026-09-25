package com.example.similarproducts.application.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class SimilarProductsException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final HttpStatus errorCode;

	public SimilarProductsException(String message, HttpStatus errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public SimilarProductsException(String message) {
		super(message);
		this.errorCode = null;
	}

}
