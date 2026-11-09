package com.devsuperior.dscommerce.authentication.exception;

import java.io.Serial;

import com.devsuperior.dscommerce.exception.InvalidRequestException;

public class UnsupportedGrantTypeException extends InvalidRequestException {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Grant type must be password.";

	public UnsupportedGrantTypeException() {
		super(MESSAGE);
	}

}
