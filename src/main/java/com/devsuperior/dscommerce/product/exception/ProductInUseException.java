package com.devsuperior.dscommerce.product.exception;

import java.io.Serial;

import com.devsuperior.dscommerce.exception.BusinessRuleException;

public class ProductInUseException extends BusinessRuleException {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Product cannot be deleted because it is in orders.";

	public ProductInUseException() {
		super(MESSAGE);
	}

}
