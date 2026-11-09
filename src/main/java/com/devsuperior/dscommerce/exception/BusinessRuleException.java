package com.devsuperior.dscommerce.exception;

import java.io.Serial;

public abstract class BusinessRuleException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	protected BusinessRuleException(String message) {
		super(message);
	}

}
