package com.devsuperior.dscommerce.order.entity;

import java.util.Objects;

import com.devsuperior.dscommerce.product.entity.Product;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class OrderItemPK {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	protected OrderItemPK() {
	}

	OrderItemPK(Order order, Product product) {
		this.order = order;
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof OrderItemPK other)) {
			return false;
		}
		return Objects.equals(order, other.order) && Objects.equals(product, other.product);
	}

	@Override
	public int hashCode() {
		return Objects.hash(order, product);
	}

}
