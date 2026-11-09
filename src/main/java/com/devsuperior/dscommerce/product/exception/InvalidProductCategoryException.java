package com.devsuperior.dscommerce.product.exception;

import java.io.Serial;

import com.devsuperior.dscommerce.exception.InvalidRequestException;

public class InvalidProductCategoryException extends InvalidRequestException {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Product has a category that does not exist.";

	public InvalidProductCategoryException() {
		super(MESSAGE);
	}

}
