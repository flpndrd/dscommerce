package com.devsuperior.dscommerce.order.exception;

import java.io.Serial;

import com.devsuperior.dscommerce.exception.InvalidRequestException;

public class RepeatedOrderProductException extends InvalidRequestException {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Order has repeated products.";

	public RepeatedOrderProductException() {
		super(MESSAGE);
	}

}
