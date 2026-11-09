package com.devsuperior.dscommerce.order.dto;

import java.time.Instant;

import com.devsuperior.dscommerce.order.entity.Payment;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentDTO(
		@JsonProperty("id")
		Long paymentId,
		Instant moment) {

	public static PaymentDTO from(Payment payment) {
		return new PaymentDTO(payment.getPaymentId(), payment.getMoment());
	}

}
