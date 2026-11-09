package com.devsuperior.dscommerce.user.exception;

import java.io.Serial;

import com.devsuperior.dscommerce.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "User not found.";

	public UserNotFoundException() {
		super(MESSAGE);
	}

}
