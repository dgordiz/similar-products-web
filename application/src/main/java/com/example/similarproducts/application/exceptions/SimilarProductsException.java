package com.example.similarproducts.application.exceptions;

import lombok.Getter;

@Getter
public class SimilarProductsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;

	public SimilarProductsException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public SimilarProductsException(String message) {
		super(message);
		this.errorCode = null;
	}

}
