package com.devsuperior.dscommerce.product.exception;

import java.io.Serial;

import com.devsuperior.dscommerce.exception.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Product not found.";

	public ProductNotFoundException() {
		super(MESSAGE);
	}

}
