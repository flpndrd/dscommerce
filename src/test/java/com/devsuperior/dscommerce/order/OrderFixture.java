package com.devsuperior.dscommerce.order;

import java.time.Instant;

import org.springframework.test.util.ReflectionTestUtils;

import com.devsuperior.dscommerce.order.entity.Order;
import com.devsuperior.dscommerce.order.entity.OrderStatus;
import com.devsuperior.dscommerce.product.ProductFixture;
import com.devsuperior.dscommerce.user.UserFixture;

public final class OrderFixture {

	public static final Long CLIENT_ORDER_ID = 1L;

	public static final Long ANOTHER_CLIENT_ORDER_ID = 2L;

	public static final Long NONEXISTENT_ORDER_ID = 999L;

	private OrderFixture() {
	}

	public static Order aClientOrder() {
		Order order = new Order(Instant.parse("2022-07-25T13:00:00Z"), OrderStatus.PAID, UserFixture.aClientUser());
		ReflectionTestUtils.setField(order, "orderId", CLIENT_ORDER_ID);
		order.addItem(ProductFixture.anOrderedProduct(), 2);
		return order;
	}

}
