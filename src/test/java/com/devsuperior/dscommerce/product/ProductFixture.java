package com.devsuperior.dscommerce.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import com.devsuperior.dscommerce.category.CategoryFixture;
import com.devsuperior.dscommerce.category.dto.CategoryDTO;
import com.devsuperior.dscommerce.product.dto.ProductDTO;
import com.devsuperior.dscommerce.product.entity.Product;

public final class ProductFixture {

	public static final Long ORDERED_PRODUCT_ID = 1L;

	public static final Long UNORDERED_PRODUCT_ID = 4L;

	public static final Long NONEXISTENT_PRODUCT_ID = 999L;

	public static final BigDecimal ORDERED_PRODUCT_PRICE = new BigDecimal("90.50");

	private ProductFixture() {
	}

	public static Product anOrderedProduct() {
		String lordOfTheRingsImageUrl = "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg";
		Product product = new Product(
				"The Lord of the Rings",
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
				ORDERED_PRODUCT_PRICE,
				lordOfTheRingsImageUrl);
		ReflectionTestUtils.setField(product, "productId", ORDERED_PRODUCT_ID);
		product.getCategories().add(CategoryFixture.aBooksCategory());
		return product;
	}

	public static ProductDTO aNewProductDTO() {
		return aNewProductDTOWithCategories(List.of(new CategoryDTO(CategoryFixture.BOOKS_CATEGORY_ID, null)));
	}

	public static ProductDTO aNewProductDTOWithCategories(List<CategoryDTO> categories) {
		return new ProductDTO(
				null,
				"The Hobbit",
				"A fantasy novel by J. R. R. Tolkien.",
				new BigDecimal("75.00"),
				"https://example.com/the-hobbit.jpg",
				categories);
	}

}
