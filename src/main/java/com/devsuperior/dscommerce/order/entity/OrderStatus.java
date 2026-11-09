package com.devsuperior.dscommerce.order.entity;

import jakarta.persistence.EnumeratedValue;

public enum OrderStatus {

	WAITING_PAYMENT(0),
	PAID(1),
	SHIPPED(2),
	DELIVERED(3),
	CANCELED(4);

	@EnumeratedValue
	private final int code;

	OrderStatus(int code) {
		this.code = code;
	}

}
