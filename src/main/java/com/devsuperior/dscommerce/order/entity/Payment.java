package com.devsuperior.dscommerce.order.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_payment")
public class Payment {

	@Id
	private Long paymentId;

	private Instant moment;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "order_id")
	private Order order;

	protected Payment() {
	}

	public Long getPaymentId() {
		return paymentId;
	}

	public Instant getMoment() {
		return moment;
	}

}
