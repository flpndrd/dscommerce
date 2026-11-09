package com.devsuperior.dscommerce.exception;

import java.io.Serial;

public abstract class InvalidRequestException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	protected InvalidRequestException(String message) {
		super(message);
	}

}
