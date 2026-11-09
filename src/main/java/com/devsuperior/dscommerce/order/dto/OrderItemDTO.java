package com.devsuperior.dscommerce.order.dto;

import java.math.BigDecimal;

import com.devsuperior.dscommerce.order.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemDTO(
		@NotNull(message = "Product id is required")
		Long productId,
		String name,
		BigDecimal price,
		@NotNull(message = "Quantity is required")
		@Positive(message = "Quantity must be positive")
		Integer quantity,
		@JsonProperty("imgUrl")
		String imageUrl) {

	public static OrderItemDTO from(OrderItem orderItem) {
		return new OrderItemDTO(
				orderItem.getProduct().getProductId(),
				orderItem.getProduct().getName(),
				orderItem.getPrice(),
				orderItem.getQuantity(),
				orderItem.getProduct().getImageUrl());
	}

	@JsonProperty("subTotal")
	public BigDecimal subTotal() {
		return price.multiply(BigDecimal.valueOf(quantity));
	}

}
