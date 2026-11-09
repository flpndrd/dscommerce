package com.devsuperior.dscommerce.order.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.accesstoken.AccessTokenSupport;
import com.devsuperior.dscommerce.order.OrderFixture;
import com.devsuperior.dscommerce.product.ProductFixture;
import com.devsuperior.dscommerce.security.SecurityProperties;
import com.devsuperior.dscommerce.user.UserFixture;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIT {

	private final MockMvcTester mockMvcTester;

	private final AccessTokenSupport accessTokenSupport;

	@Autowired
	OrderControllerIT(MockMvcTester mockMvcTester, SecurityProperties securityProperties) {
		this.mockMvcTester = mockMvcTester;
		this.accessTokenSupport = new AccessTokenSupport(mockMvcTester, securityProperties);
	}

	@Nested
	class Find {

		@Test
		void shouldReturnOrderOfAnyClientWhenUserIsAdmin() {
			MvcTestResult mvcTestResult = getOrder(adminToken(), OrderFixture.CLIENT_ORDER_ID);

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.client.name").isEqualTo("Maria Brown");
			assertThat(mvcTestResult).bodyJson().extractingPath("$.payment.id").isNotNull();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.items.length()").isEqualTo(2);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.total").isEqualTo(1431.0);
		}

		@Test
		void shouldReturnOwnOrderWhenUserIsClient() {
			MvcTestResult mvcTestResult = getOrder(clientToken(), OrderFixture.CLIENT_ORDER_ID);

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.client.id")
					.convertTo(Long.class)
					.isEqualTo(UserFixture.CLIENT_USER_ID);
		}

		@Test
		void shouldReturnNotFoundWhenOrderBelongsToAnotherClient() {
			MvcTestResult mvcTestResult = getOrder(clientToken(), OrderFixture.ANOTHER_CLIENT_ORDER_ID);

			assertThat(mvcTestResult).hasStatus(HttpStatus.NOT_FOUND);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.detail").isEqualTo("Order not found.");
		}

		@Test
		void shouldReturnNotFoundWhenOrderDoesNotExist() {
			MvcTestResult mvcTestResult = getOrder(adminToken(), OrderFixture.NONEXISTENT_ORDER_ID);

			assertThat(mvcTestResult).hasStatus(HttpStatus.NOT_FOUND);
		}

		@Test
		void shouldReturnUnauthorizedWhenTokenIsMissing() {
			MvcTestResult mvcTestResult = mockMvcTester
					.get()
					.uri("/orders/{orderId}", OrderFixture.CLIENT_ORDER_ID)
					.exchange();

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNAUTHORIZED);
		}

	}

	@Nested
	class Create {

		@Test
		void shouldCreateWaitingPaymentOrderWhenUserIsClient() {
			MvcTestResult mvcTestResult = postOrder(
					clientToken(),
					anOrderRequestWith(ProductFixture.ORDERED_PRODUCT_ID));

			assertThat(mvcTestResult).hasStatus(HttpStatus.CREATED);
			assertThat(mvcTestResult).headers().containsHeader(HttpHeaders.LOCATION);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.status").isEqualTo("WAITING_PAYMENT");
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.client.id")
					.convertTo(Long.class)
					.isEqualTo(UserFixture.CLIENT_USER_ID);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.payment").isNull();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.items[0].subTotal").isEqualTo(181.0);
		}

		@Test
		void shouldReturnUnprocessableContentWhenOrderHasNoItems() {
			MvcTestResult mvcTestResult = postOrder(clientToken(), """
					{"items": []}
					""");

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.errors[0].fieldName").isEqualTo("items");
		}

		@Test
		void shouldReturnBadRequestWhenProductDoesNotExist() {
			MvcTestResult mvcTestResult = postOrder(
					clientToken(),
					anOrderRequestWith(ProductFixture.NONEXISTENT_PRODUCT_ID));

			assertThat(mvcTestResult).hasStatus(HttpStatus.BAD_REQUEST);
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.detail")
					.isEqualTo("Order has a product that does not exist.");
		}

		@Test
		void shouldReturnBadRequestWhenProductIsRepeated() {
			MvcTestResult mvcTestResult = postOrder(clientToken(), """
					{"items": [{"productId": 1, "quantity": 1}, {"productId": 1, "quantity": 2}]}
					""");

			assertThat(mvcTestResult).hasStatus(HttpStatus.BAD_REQUEST);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.detail").isEqualTo("Order has repeated products.");
		}

		@Test
		void shouldReturnUnauthorizedWhenTokenIsMissing() {
			MvcTestResult mvcTestResult = mockMvcTester
					.post()
					.uri("/orders")
					.contentType(MediaType.APPLICATION_JSON)
					.content(anOrderRequestWith(ProductFixture.ORDERED_PRODUCT_ID))
					.exchange();

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNAUTHORIZED);
		}

	}

	private MvcTestResult getOrder(String accessToken, Long orderId) {
		return mockMvcTester
				.get()
				.uri("/orders/{orderId}", orderId)
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.exchange();
	}

	private MvcTestResult postOrder(String accessToken, String orderRequest) {
		return mockMvcTester
				.post()
				.uri("/orders")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(orderRequest)
				.exchange();
	}

	private String adminToken() {
		return accessTokenSupport.obtainAccessToken(UserFixture.ADMIN_EMAIL, UserFixture.PASSWORD);
	}

	private String clientToken() {
		return accessTokenSupport.obtainAccessToken(UserFixture.CLIENT_EMAIL, UserFixture.PASSWORD);
	}

	private static String anOrderRequestWith(Long productId) {
		return """
				{"items": [{"productId": %d, "quantity": 2}]}
				""".formatted(productId);
	}

}
