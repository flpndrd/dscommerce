package com.devsuperior.dscommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devsuperior.dscommerce.order.OrderFixture;
import com.devsuperior.dscommerce.order.dto.OrderDTO;
import com.devsuperior.dscommerce.order.dto.OrderItemDTO;
import com.devsuperior.dscommerce.order.entity.Order;
import com.devsuperior.dscommerce.order.entity.OrderStatus;
import com.devsuperior.dscommerce.order.exception.InvalidOrderProductException;
import com.devsuperior.dscommerce.order.exception.OrderNotFoundException;
import com.devsuperior.dscommerce.order.exception.RepeatedOrderProductException;
import com.devsuperior.dscommerce.order.repository.OrderRepository;
import com.devsuperior.dscommerce.product.ProductFixture;
import com.devsuperior.dscommerce.product.repository.ProductRepository;
import com.devsuperior.dscommerce.security.AuthenticatedUser;
import com.devsuperior.dscommerce.user.UserFixture;
import com.devsuperior.dscommerce.user.exception.UserNotFoundException;
import com.devsuperior.dscommerce.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	private static final Instant NOW = Instant.parse("2026-10-31T12:00:00Z");

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthenticatedUser authenticatedUser;

	private OrderService orderService;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
		orderService = new OrderService(
				orderRepository,
				productRepository,
				userRepository,
				authenticatedUser,
				fixedClock);
	}

	@Nested
	class Find {

		@Test
		void shouldReturnAnyOrderWhenUserIsAdmin() {
			given(authenticatedUser.isAdmin()).willReturn(true);
			given(orderRepository.findWithItemsByOrderId(OrderFixture.CLIENT_ORDER_ID))
					.willReturn(Optional.of(OrderFixture.aClientOrder()));

			OrderDTO orderDTO = orderService.find(OrderFixture.CLIENT_ORDER_ID);

			assertThat(orderDTO.orderId()).isEqualTo(OrderFixture.CLIENT_ORDER_ID);
			assertThat(orderDTO.total()).isEqualByComparingTo("181.00");
		}

		@Test
		void shouldReturnOwnOrderWhenUserIsClient() {
			given(authenticatedUser.isAdmin()).willReturn(false);
			given(authenticatedUser.getEmail()).willReturn(UserFixture.CLIENT_EMAIL);
			given(orderRepository.findWithItemsByOrderIdAndClientEmail(
					OrderFixture.CLIENT_ORDER_ID,
					UserFixture.CLIENT_EMAIL))
					.willReturn(Optional.of(OrderFixture.aClientOrder()));

			OrderDTO orderDTO = orderService.find(OrderFixture.CLIENT_ORDER_ID);

			assertThat(orderDTO.client().userId()).isEqualTo(UserFixture.CLIENT_USER_ID);
		}

		@Test
		void shouldThrowOrderNotFoundExceptionWhenOrderBelongsToAnotherClient() {
			given(authenticatedUser.isAdmin()).willReturn(false);
			given(authenticatedUser.getEmail()).willReturn(UserFixture.CLIENT_EMAIL);
			given(orderRepository.findWithItemsByOrderIdAndClientEmail(
					OrderFixture.ANOTHER_CLIENT_ORDER_ID,
					UserFixture.CLIENT_EMAIL))
					.willReturn(Optional.empty());

			assertThatThrownBy(() -> orderService.find(OrderFixture.ANOTHER_CLIENT_ORDER_ID))
					.isInstanceOf(OrderNotFoundException.class);
		}

	}

	@Nested
	class Create {

		@Test
		void shouldSaveWaitingPaymentOrderWithProductPriceWhenProductsExist() {
			given(authenticatedUser.getEmail()).willReturn(UserFixture.CLIENT_EMAIL);
			given(userRepository.findByEmail(UserFixture.CLIENT_EMAIL))
					.willReturn(Optional.of(UserFixture.aClientUser()));
			given(productRepository.findAllById(List.of(ProductFixture.ORDERED_PRODUCT_ID)))
					.willReturn(List.of(ProductFixture.anOrderedProduct()));
			given(orderRepository.save(any(Order.class))).will(returnsFirstArg());

			OrderDTO orderDTO = orderService.create(anOrderDTOWith(ProductFixture.ORDERED_PRODUCT_ID));

			assertThat(orderDTO.moment()).isEqualTo(NOW);
			assertThat(orderDTO.status()).isEqualTo(OrderStatus.WAITING_PAYMENT);
			assertThat(orderDTO.client().userId()).isEqualTo(UserFixture.CLIENT_USER_ID);
			assertThat(orderDTO.payment()).isNull();
			assertThat(orderDTO.items().getFirst().price()).isEqualByComparingTo(ProductFixture.ORDERED_PRODUCT_PRICE);
			assertThat(orderDTO.total()).isEqualByComparingTo("181.00");
		}

		@Test
		void shouldThrowInvalidOrderProductExceptionWhenProductDoesNotExist() {
			given(authenticatedUser.getEmail()).willReturn(UserFixture.CLIENT_EMAIL);
			given(userRepository.findByEmail(UserFixture.CLIENT_EMAIL))
					.willReturn(Optional.of(UserFixture.aClientUser()));
			given(productRepository.findAllById(List.of(ProductFixture.NONEXISTENT_PRODUCT_ID))).willReturn(List.of());

			assertThatThrownBy(() -> orderService.create(anOrderDTOWith(ProductFixture.NONEXISTENT_PRODUCT_ID)))
					.isInstanceOf(InvalidOrderProductException.class);
			then(orderRepository).should(never()).save(any(Order.class));
		}

		@Test
		void shouldThrowRepeatedOrderProductExceptionWhenProductIsRepeated() {
			OrderItemDTO orderItemDTO = new OrderItemDTO(ProductFixture.ORDERED_PRODUCT_ID, null, null, 1, null);
			OrderDTO orderDTO = new OrderDTO(null, null, null, null, null, List.of(orderItemDTO, orderItemDTO));

			assertThatThrownBy(() -> orderService.create(orderDTO)).isInstanceOf(RepeatedOrderProductException.class);
			then(orderRepository).should(never()).save(any(Order.class));
		}

		@Test
		void shouldThrowUserNotFoundExceptionWhenAuthenticatedUserDoesNotExist() {
			given(authenticatedUser.getEmail()).willReturn(UserFixture.CLIENT_EMAIL);
			given(userRepository.findByEmail(UserFixture.CLIENT_EMAIL)).willReturn(Optional.empty());

			assertThatThrownBy(() -> orderService.create(anOrderDTOWith(ProductFixture.ORDERED_PRODUCT_ID)))
					.isInstanceOf(UserNotFoundException.class);
		}

	}

	private static OrderDTO anOrderDTOWith(Long productId) {
		return new OrderDTO(null, null, null, null, null, List.of(new OrderItemDTO(productId, null, null, 2, null)));
	}

}
