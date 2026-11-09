package com.devsuperior.dscommerce.order.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.order.OrderFixture;
import com.devsuperior.dscommerce.product.ProductFixture;
import com.devsuperior.dscommerce.product.entity.Product;

class OrderItemPKTest {

	private final Order order = OrderFixture.aClientOrder();

	private final Product product = ProductFixture.anOrderedProduct();

	@Nested
	class EqualsAndHashCode {

		@Test
		void shouldBeEqualWhenOrderAndProductAreTheSame() {
			OrderItemPK orderItemPK = new OrderItemPK(order, product);
			OrderItemPK sameOrderItemPK = new OrderItemPK(order, product);

			boolean equal = orderItemPK.equals(sameOrderItemPK);

			assertThat(equal).isTrue();
			assertThat(orderItemPK.hashCode()).isEqualTo(sameOrderItemPK.hashCode());
		}

		@Test
		void shouldBeEqualWhenComparedWithItself() {
			OrderItemPK orderItemPK = new OrderItemPK(order, product);

			boolean equal = orderItemPK.equals(orderItemPK);

			assertThat(equal).isTrue();
		}

		@Test
		void shouldNotBeEqualWhenProductIsDifferent() {
			OrderItemPK orderItemPK = new OrderItemPK(order, product);

			boolean equal = orderItemPK.equals(new OrderItemPK(order, ProductFixture.anOrderedProduct()));

			assertThat(equal).isFalse();
		}

		@Test
		void shouldNotBeEqualWhenOrderIsDifferent() {
			OrderItemPK orderItemPK = new OrderItemPK(order, product);

			boolean equal = orderItemPK.equals(new OrderItemPK(OrderFixture.aClientOrder(), product));

			assertThat(equal).isFalse();
		}

		@Test
		void shouldNotBeEqualWhenObjectIsNotOrderItemPK() {
			OrderItemPK orderItemPK = new OrderItemPK(order, product);
			Object object = new Object();

			boolean equal = orderItemPK.equals(object);

			assertThat(equal).isFalse();
		}

	}

}
