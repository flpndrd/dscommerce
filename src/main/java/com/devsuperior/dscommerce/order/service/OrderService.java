package com.devsuperior.dscommerce.order.service;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.order.dto.OrderDTO;
import com.devsuperior.dscommerce.order.dto.OrderItemDTO;
import com.devsuperior.dscommerce.order.entity.Order;
import com.devsuperior.dscommerce.order.entity.OrderStatus;
import com.devsuperior.dscommerce.order.exception.InvalidOrderProductException;
import com.devsuperior.dscommerce.order.exception.OrderNotFoundException;
import com.devsuperior.dscommerce.order.exception.RepeatedOrderProductException;
import com.devsuperior.dscommerce.order.repository.OrderRepository;
import com.devsuperior.dscommerce.product.entity.Product;
import com.devsuperior.dscommerce.product.repository.ProductRepository;
import com.devsuperior.dscommerce.security.AuthenticatedUser;
import com.devsuperior.dscommerce.user.entity.User;
import com.devsuperior.dscommerce.user.exception.UserNotFoundException;
import com.devsuperior.dscommerce.user.repository.UserRepository;

@Service
public class OrderService {

	private final OrderRepository orderRepository;

	private final ProductRepository productRepository;

	private final UserRepository userRepository;

	private final AuthenticatedUser authenticatedUser;

	private final Clock clock;

	public OrderService(
			OrderRepository orderRepository,
			ProductRepository productRepository,
			UserRepository userRepository,
			AuthenticatedUser authenticatedUser,
			Clock clock) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.userRepository = userRepository;
		this.authenticatedUser = authenticatedUser;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public OrderDTO find(Long orderId) {
		Optional<Order> order = authenticatedUser.isAdmin()
				? orderRepository.findWithItemsByOrderId(orderId)
				: orderRepository.findWithItemsByOrderIdAndClientEmail(orderId, authenticatedUser.getEmail());

		return OrderDTO.from(order.orElseThrow(() -> new OrderNotFoundException()));
	}

	@Transactional
	public OrderDTO create(OrderDTO orderDTO) {
		List<Long> productIds = orderDTO.items().stream().map(orderItemDTO -> orderItemDTO.productId()).toList();

		if (hasRepeatedProducts(productIds)) {
			throw new RepeatedOrderProductException();
		}
		User client = userRepository
				.findByEmail(authenticatedUser.getEmail())
				.orElseThrow(() -> new UserNotFoundException());

		Map<Long, Product> productsById = findProductsById(productIds);
		Order order = new Order(clock.instant(), OrderStatus.WAITING_PAYMENT, client);

		for (OrderItemDTO orderItemDTO : orderDTO.items()) {
			Product product = productsById.get(orderItemDTO.productId());

			if (product == null) {
				throw new InvalidOrderProductException();
			}
			order.addItem(product, orderItemDTO.quantity());
		}
		Order savedOrder = orderRepository.save(order);
		return OrderDTO.from(savedOrder);
	}

	private boolean hasRepeatedProducts(List<Long> productIds) {
		return new HashSet<>(productIds).size() != productIds.size();
	}

	private Map<Long, Product> findProductsById(List<Long> productIds) {
		return productRepository
				.findAllById(productIds)
				.stream()
				.collect(Collectors.toMap(product -> product.getProductId(), Function.identity()));
	}

}
