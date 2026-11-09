package com.devsuperior.dscommerce.exception;

import java.io.Serial;

public abstract class ResourceNotFoundException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	protected ResourceNotFoundException(String message) {
		super(message);
	}

}
