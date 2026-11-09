package com.devsuperior.dscommerce.order.exception;

import java.io.Serial;

import com.devsuperior.dscommerce.exception.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Order not found.";

	public OrderNotFoundException() {
		super(MESSAGE);
	}

}
