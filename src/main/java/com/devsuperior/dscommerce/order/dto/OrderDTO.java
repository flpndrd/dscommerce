package com.devsuperior.dscommerce.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.devsuperior.dscommerce.order.entity.Order;
import com.devsuperior.dscommerce.order.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record OrderDTO(
		@JsonProperty("id")
		Long orderId,
		Instant moment,
		OrderStatus status,
		ClientDTO client,
		PaymentDTO payment,
		@NotEmpty(message = "At least one item is required")
		List<@Valid OrderItemDTO> items) {

	public static OrderDTO from(Order order) {
		return new OrderDTO(
				order.getOrderId(),
				order.getMoment(),
				order.getStatus(),
				ClientDTO.from(order.getClient()),
				order.getPayment() != null ? PaymentDTO.from(order.getPayment()) : null,
				order.getItems().stream().map(orderItem -> OrderItemDTO.from(orderItem)).toList());
	}

	@JsonProperty("total")
	public BigDecimal total() {
		return items
				.stream()
				.map(orderItemDTO -> orderItemDTO.subTotal())
				.reduce(BigDecimal.ZERO, (total, subTotal) -> total.add(subTotal));
	}

}
