package com.devsuperior.dscommerce.product.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

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
import com.devsuperior.dscommerce.category.CategoryFixture;
import com.devsuperior.dscommerce.category.dto.CategoryDTO;
import com.devsuperior.dscommerce.product.ProductFixture;
import com.devsuperior.dscommerce.product.dto.ProductDTO;
import com.devsuperior.dscommerce.security.SecurityProperties;
import com.devsuperior.dscommerce.user.UserFixture;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIT {

	private final MockMvcTester mockMvcTester;

	private final JsonMapper jsonMapper;

	private final AccessTokenSupport accessTokenSupport;

	@Autowired
	ProductControllerIT(MockMvcTester mockMvcTester, JsonMapper jsonMapper, SecurityProperties securityProperties) {
		this.mockMvcTester = mockMvcTester;
		this.jsonMapper = jsonMapper;
		this.accessTokenSupport = new AccessTokenSupport(mockMvcTester, securityProperties);
	}

	@Nested
	class Find {

		@Test
		void shouldReturnProductWithCategoriesWhenTokenIsMissing() {
			MvcTestResult mvcTestResult = mockMvcTester
					.get()
					.uri("/products/{productId}", ProductFixture.ORDERED_PRODUCT_ID)
					.exchange();

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.name").isEqualTo("The Lord of the Rings");
			assertThat(mvcTestResult).bodyJson().extractingPath("$.imgUrl").asString().endsWith("1-big.jpg");
			assertThat(mvcTestResult).bodyJson().extractingPath("$.categories[0].name").isEqualTo("Livros");
		}

		@Test
		void shouldReturnNotFoundWhenProductDoesNotExist() {
			MvcTestResult mvcTestResult = mockMvcTester
					.get()
					.uri("/products/{productId}", ProductFixture.NONEXISTENT_PRODUCT_ID)
					.exchange();

			assertThat(mvcTestResult).hasStatus(HttpStatus.NOT_FOUND);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.detail").isEqualTo("Product not found.");
		}

	}

	@Nested
	class ListProducts {

		@Test
		void shouldReturnProductPageWhenTokenIsMissing() {
			MvcTestResult mvcTestResult = mockMvcTester.get().uri("/products?page=0&size=12&sort=name").exchange();

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.content.length()").isEqualTo(12);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.content[0].name").isEqualTo("Macbook Pro");
			assertThat(mvcTestResult).bodyJson().extractingPath("$.page.totalElements").isEqualTo(25);
		}

		@Test
		void shouldReturnOnlyMatchingProductsWhenNameIsInformed() {
			MvcTestResult mvcTestResult = mockMvcTester.get().uri("/products?name=MAC").exchange();

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.content[*].name")
					.asArray()
					.containsExactly("Macbook Pro");
		}

	}

	@Nested
	class Create {

		@Test
		void shouldCreateProductWhenUserIsAdmin() {
			MvcTestResult mvcTestResult = postProduct(adminToken(), ProductFixture.aNewProductDTO());

			assertThat(mvcTestResult).hasStatus(HttpStatus.CREATED);
			assertThat(mvcTestResult).headers().containsHeader(HttpHeaders.LOCATION);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.id").isNotNull();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.name").isEqualTo("The Hobbit");
			assertThat(mvcTestResult).bodyJson().extractingPath("$.categories[0].name").isEqualTo("Livros");
		}

		@Test
		void shouldReturnUnprocessableContentWhenNameIsTooShort() {
			ProductDTO productDTO = aProductDTO("Ho", "A fantasy novel by Tolkien.", new BigDecimal("75.00"));

			MvcTestResult mvcTestResult = postProduct(adminToken(), productDTO);

			assertUnprocessableContentOn(mvcTestResult, "name");
		}

		@Test
		void shouldReturnUnprocessableContentWhenDescriptionIsTooShort() {
			ProductDTO productDTO = aProductDTO("The Hobbit", "A novel", new BigDecimal("75.00"));

			MvcTestResult mvcTestResult = postProduct(adminToken(), productDTO);

			assertUnprocessableContentOn(mvcTestResult, "description");
		}

		@Test
		void shouldReturnUnprocessableContentWhenPriceIsNegative() {
			ProductDTO productDTO = aProductDTO("The Hobbit", "A fantasy novel by Tolkien.", new BigDecimal("-1.00"));

			MvcTestResult mvcTestResult = postProduct(adminToken(), productDTO);

			assertUnprocessableContentOn(mvcTestResult, "price");
		}

		@Test
		void shouldReturnUnprocessableContentWhenPriceIsZero() {
			ProductDTO productDTO = aProductDTO("The Hobbit", "A fantasy novel by Tolkien.", BigDecimal.ZERO);

			MvcTestResult mvcTestResult = postProduct(adminToken(), productDTO);

			assertUnprocessableContentOn(mvcTestResult, "price");
		}

		@Test
		void shouldReturnUnprocessableContentWhenProductHasNoCategory() {
			ProductDTO productDTO = ProductFixture.aNewProductDTOWithCategories(List.of());

			MvcTestResult mvcTestResult = postProduct(adminToken(), productDTO);

			assertUnprocessableContentOn(mvcTestResult, "categories");
		}

		@Test
		void shouldReturnBadRequestWhenCategoryDoesNotExist() {
			ProductDTO productDTO = ProductFixture.aNewProductDTOWithCategories(List.of(
					new CategoryDTO(CategoryFixture.NONEXISTENT_CATEGORY_ID, null)));

			MvcTestResult mvcTestResult = postProduct(adminToken(), productDTO);

			assertThat(mvcTestResult).hasStatus(HttpStatus.BAD_REQUEST);
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.detail")
					.isEqualTo("Product has a category that does not exist.");
		}

		@Test
		void shouldReturnForbiddenWhenUserIsClient() {
			MvcTestResult mvcTestResult = postProduct(clientToken(), ProductFixture.aNewProductDTO());

			assertThat(mvcTestResult).hasStatus(HttpStatus.FORBIDDEN);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.detail").isEqualTo("Access denied.");
		}

		@Test
		void shouldReturnUnauthorizedWhenTokenIsInvalid() {
			MvcTestResult mvcTestResult = postProduct("invalid-token", ProductFixture.aNewProductDTO());

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNAUTHORIZED);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.status").isEqualTo(401);
		}

	}

	@Nested
	class Update {

		@Test
		void shouldUpdateProductWhenUserIsAdmin() {
			MvcTestResult mvcTestResult = mockMvcTester
					.put()
					.uri("/products/{productId}", ProductFixture.UNORDERED_PRODUCT_ID)
					.headers(httpHeaders -> httpHeaders.setBearerAuth(adminToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(ProductFixture.aNewProductDTO()))
					.exchange();

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.id")
					.convertTo(Long.class)
					.isEqualTo(ProductFixture.UNORDERED_PRODUCT_ID);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.name").isEqualTo("The Hobbit");
		}

		@Test
		void shouldReturnNotFoundWhenProductDoesNotExist() {
			MvcTestResult mvcTestResult = mockMvcTester
					.put()
					.uri("/products/{productId}", ProductFixture.NONEXISTENT_PRODUCT_ID)
					.headers(httpHeaders -> httpHeaders.setBearerAuth(adminToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonMapper.writeValueAsString(ProductFixture.aNewProductDTO()))
					.exchange();

			assertThat(mvcTestResult).hasStatus(HttpStatus.NOT_FOUND);
		}

	}

	@Nested
	class Delete {

		@Test
		void shouldDeleteProductWhenProductIsNotInOrders() {
			MvcTestResult mvcTestResult = deleteProduct(adminToken(), ProductFixture.UNORDERED_PRODUCT_ID);

			assertThat(mvcTestResult).hasStatus(HttpStatus.NO_CONTENT);
		}

		@Test
		void shouldReturnNotFoundWhenProductDoesNotExist() {
			MvcTestResult mvcTestResult = deleteProduct(adminToken(), ProductFixture.NONEXISTENT_PRODUCT_ID);

			assertThat(mvcTestResult).hasStatus(HttpStatus.NOT_FOUND);
		}

		@Test
		void shouldReturnConflictWhenProductIsInOrders() {
			MvcTestResult mvcTestResult = deleteProduct(adminToken(), ProductFixture.ORDERED_PRODUCT_ID);

			assertThat(mvcTestResult).hasStatus(HttpStatus.CONFLICT);
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.detail")
					.isEqualTo("Product cannot be deleted because it is in orders.");
		}

		@Test
		void shouldReturnForbiddenWhenUserIsClient() {
			MvcTestResult mvcTestResult = deleteProduct(clientToken(), ProductFixture.UNORDERED_PRODUCT_ID);

			assertThat(mvcTestResult).hasStatus(HttpStatus.FORBIDDEN);
		}

		@Test
		void shouldReturnUnauthorizedWhenTokenIsMissing() {
			MvcTestResult mvcTestResult = mockMvcTester
					.delete()
					.uri("/products/{productId}", ProductFixture.UNORDERED_PRODUCT_ID)
					.exchange();

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNAUTHORIZED);
		}

	}

	private MvcTestResult postProduct(String accessToken, ProductDTO productDTO) {
		return mockMvcTester
				.post()
				.uri("/products")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(productDTO))
				.exchange();
	}

	private MvcTestResult deleteProduct(String accessToken, Long productId) {
		return mockMvcTester
				.delete()
				.uri("/products/{productId}", productId)
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.exchange();
	}

	private String adminToken() {
		return accessTokenSupport.obtainAccessToken(UserFixture.ADMIN_EMAIL, UserFixture.PASSWORD);
	}

	private String clientToken() {
		return accessTokenSupport.obtainAccessToken(UserFixture.CLIENT_EMAIL, UserFixture.PASSWORD);
	}

	private static ProductDTO aProductDTO(String name, String description, BigDecimal price) {
		return new ProductDTO(
				null,
				name,
				description,
				price,
				null,
				List.of(new CategoryDTO(CategoryFixture.BOOKS_CATEGORY_ID, null)));
	}

	private static void assertUnprocessableContentOn(MvcTestResult mvcTestResult, String fieldName) {
		assertThat(mvcTestResult).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
		assertThat(mvcTestResult).bodyJson().extractingPath("$.errors[0].fieldName").isEqualTo(fieldName);
	}

}
