package com.devsuperior.dscommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.devsuperior.dscommerce.category.CategoryFixture;
import com.devsuperior.dscommerce.category.dto.CategoryDTO;
import com.devsuperior.dscommerce.category.repository.CategoryRepository;
import com.devsuperior.dscommerce.product.ProductFixture;
import com.devsuperior.dscommerce.product.dto.ProductDTO;
import com.devsuperior.dscommerce.product.dto.ProductSummaryDTO;
import com.devsuperior.dscommerce.product.entity.Product;
import com.devsuperior.dscommerce.product.exception.InvalidProductCategoryException;
import com.devsuperior.dscommerce.product.exception.ProductInUseException;
import com.devsuperior.dscommerce.product.exception.ProductNotFoundException;
import com.devsuperior.dscommerce.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private ProductService productService;

	@Nested
	class Find {

		@Test
		void shouldReturnProductWhenProductExists() {
			given(productRepository.findWithCategoriesByProductId(ProductFixture.ORDERED_PRODUCT_ID))
					.willReturn(Optional.of(ProductFixture.anOrderedProduct()));

			ProductDTO productDTO = productService.find(ProductFixture.ORDERED_PRODUCT_ID);

			assertThat(productDTO.productId()).isEqualTo(ProductFixture.ORDERED_PRODUCT_ID);
			assertThat(productDTO.name()).isEqualTo("The Lord of the Rings");
			assertThat(productDTO.categories())
					.extracting(categoryDTO -> categoryDTO.categoryId())
					.containsExactly(CategoryFixture.BOOKS_CATEGORY_ID);
		}

		@Test
		void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
			given(productRepository.findWithCategoriesByProductId(ProductFixture.NONEXISTENT_PRODUCT_ID))
					.willReturn(Optional.empty());

			assertThatThrownBy(() -> productService.find(ProductFixture.NONEXISTENT_PRODUCT_ID))
					.isInstanceOf(ProductNotFoundException.class);
		}

	}

	@Nested
	class ListProducts {

		@Test
		void shouldReturnProductPageFilteredByName() {
			Pageable pageable = PageRequest.of(0, 12);
			Page<ProductSummaryDTO> expectedProductPage = new PageImpl<>(
					List.of(new ProductSummaryDTO(3L, "Macbook Pro", new BigDecimal("1250.00"), null)));
			given(productRepository.listByName("mac", pageable)).willReturn(expectedProductPage);

			Page<ProductSummaryDTO> productPage = productService.list("mac", pageable);

			assertThat(productPage).isEqualTo(expectedProductPage);
		}

	}

	@Nested
	class Create {

		@Test
		void shouldSaveProductWithCategoriesWhenCategoriesExist() {
			given(categoryRepository.findAllById(Set.of(CategoryFixture.BOOKS_CATEGORY_ID)))
					.willReturn(List.of(CategoryFixture.aBooksCategory()));
			given(productRepository.save(any(Product.class))).will(returnsFirstArg());

			ProductDTO productDTO = productService.create(ProductFixture.aNewProductDTO());

			assertThat(productDTO.name()).isEqualTo("The Hobbit");
			assertThat(productDTO.price()).isEqualByComparingTo("75.00");
			assertThat(productDTO.categories()).extracting(categoryDTO -> categoryDTO.name()).containsExactly("Livros");
		}

		@Test
		void shouldThrowInvalidProductCategoryExceptionWhenCategoryDoesNotExist() {
			ProductDTO productDTO = ProductFixture.aNewProductDTOWithCategories(List.of(
					new CategoryDTO(CategoryFixture.NONEXISTENT_CATEGORY_ID, null)));
			given(categoryRepository.findAllById(Set.of(CategoryFixture.NONEXISTENT_CATEGORY_ID)))
					.willReturn(List.of());

			assertThatThrownBy(() -> productService.create(productDTO))
					.isInstanceOf(InvalidProductCategoryException.class);
			then(productRepository).should(never()).save(any(Product.class));
		}

	}

	@Nested
	class Update {

		@Test
		void shouldUpdateProductWhenProductExists() {
			given(productRepository.findWithCategoriesByProductId(ProductFixture.ORDERED_PRODUCT_ID))
					.willReturn(Optional.of(ProductFixture.anOrderedProduct()));
			given(categoryRepository.findAllById(Set.of(CategoryFixture.BOOKS_CATEGORY_ID)))
					.willReturn(List.of(CategoryFixture.aBooksCategory()));

			ProductDTO productDTO = productService.update(
					ProductFixture.ORDERED_PRODUCT_ID,
					ProductFixture.aNewProductDTO());

			assertThat(productDTO.productId()).isEqualTo(ProductFixture.ORDERED_PRODUCT_ID);
			assertThat(productDTO.name()).isEqualTo("The Hobbit");
		}

		@Test
		void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
			given(productRepository.findWithCategoriesByProductId(ProductFixture.NONEXISTENT_PRODUCT_ID))
					.willReturn(Optional.empty());

			assertThatThrownBy(() -> productService.update(
					ProductFixture.NONEXISTENT_PRODUCT_ID,
					ProductFixture.aNewProductDTO()))
					.isInstanceOf(ProductNotFoundException.class);
		}

	}

	@Nested
	class Delete {

		@Test
		void shouldDeleteProductWhenProductIsNotInOrders() {
			given(productRepository.existsById(ProductFixture.UNORDERED_PRODUCT_ID)).willReturn(true);
			given(productRepository.existsInOrders(ProductFixture.UNORDERED_PRODUCT_ID)).willReturn(false);

			productService.delete(ProductFixture.UNORDERED_PRODUCT_ID);

			then(productRepository).should().deleteById(ProductFixture.UNORDERED_PRODUCT_ID);
		}

		@Test
		void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
			given(productRepository.existsById(ProductFixture.NONEXISTENT_PRODUCT_ID)).willReturn(false);

			assertThatThrownBy(() -> productService.delete(ProductFixture.NONEXISTENT_PRODUCT_ID))
					.isInstanceOf(ProductNotFoundException.class);
			then(productRepository).should(never()).deleteById(ProductFixture.NONEXISTENT_PRODUCT_ID);
		}

		@Test
		void shouldThrowProductInUseExceptionWhenProductIsInOrders() {
			given(productRepository.existsById(ProductFixture.ORDERED_PRODUCT_ID)).willReturn(true);
			given(productRepository.existsInOrders(ProductFixture.ORDERED_PRODUCT_ID)).willReturn(true);

			assertThatThrownBy(() -> productService.delete(ProductFixture.ORDERED_PRODUCT_ID))
					.isInstanceOf(ProductInUseException.class);
			then(productRepository).should(never()).deleteById(ProductFixture.ORDERED_PRODUCT_ID);
		}

	}

}
