package com.devsuperior.dscommerce.order.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.devsuperior.dscommerce.product.entity.Product;
import com.devsuperior.dscommerce.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_order")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;

	private Instant moment;

	@Enumerated(EnumType.ORDINAL)
	private OrderStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id")
	private User client;

	@OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
	private Payment payment;

	@OneToMany(mappedBy = "orderItemId.order", cascade = CascadeType.PERSIST)
	private List<OrderItem> items = new ArrayList<>();

	protected Order() {
	}

	public Order(Instant moment, OrderStatus status, User client) {
		this.moment = moment;
		this.status = status;
		this.client = client;
	}

	public void addItem(Product product, Integer quantity) {
		items.add(new OrderItem(this, product, quantity, product.getPrice()));
	}

	public Long getOrderId() {
		return orderId;
	}

	public Instant getMoment() {
		return moment;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public User getClient() {
		return client;
	}

	public Payment getPayment() {
		return payment;
	}

	public List<OrderItem> getItems() {
		return items;
	}

}
