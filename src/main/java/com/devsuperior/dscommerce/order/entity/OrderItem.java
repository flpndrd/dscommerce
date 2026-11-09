package com.devsuperior.dscommerce.order.entity;

import java.math.BigDecimal;

import com.devsuperior.dscommerce.product.entity.Product;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_order_item")
public class OrderItem {

	@EmbeddedId
	private OrderItemPK orderItemId;

	private Integer quantity;

	private BigDecimal price;

	protected OrderItem() {
	}

	OrderItem(Order order, Product product, Integer quantity, BigDecimal price) {
		this.orderItemId = new OrderItemPK(order, product);
		this.quantity = quantity;
		this.price = price;
	}

	public Product getProduct() {
		return orderItemId.getProduct();
	}

	public Integer getQuantity() {
		return quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

}
