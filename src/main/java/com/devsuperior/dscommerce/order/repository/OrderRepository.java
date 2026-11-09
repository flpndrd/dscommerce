package com.devsuperior.dscommerce.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.dscommerce.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("""
			SELECT order
			FROM Order order
			JOIN FETCH order.client
			LEFT JOIN FETCH order.payment
			JOIN FETCH order.items orderItem
			JOIN FETCH orderItem.orderItemId.product
			WHERE order.orderId = :orderId
			""")
	Optional<Order> findWithItemsByOrderId(Long orderId);

	@Query("""
			SELECT order
			FROM Order order
			JOIN FETCH order.client
			LEFT JOIN FETCH order.payment
			JOIN FETCH order.items orderItem
			JOIN FETCH orderItem.orderItemId.product
			WHERE order.orderId = :orderId
			AND order.client.email = :clientEmail
			""")
	Optional<Order> findWithItemsByOrderIdAndClientEmail(Long orderId, String clientEmail);

}
