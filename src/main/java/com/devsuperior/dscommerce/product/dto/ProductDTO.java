package com.devsuperior.dscommerce.product.dto;

import java.math.BigDecimal;
import java.util.List;

import com.devsuperior.dscommerce.category.dto.CategoryDTO;
import com.devsuperior.dscommerce.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductDTO(
		@JsonProperty("id")
		Long productId,
		@NotBlank(message = "Name is required")
		@Size(min = 3, max = 80, message = "Name must be between 3 and 80 characters")
		String name,
		@NotBlank(message = "Description is required")
		@Size(min = 10, message = "Description must have at least 10 characters")
		String description,
		@NotNull(message = "Price is required")
		@Positive(message = "Price must be positive")
		BigDecimal price,
		@JsonProperty("imgUrl")
		String imageUrl,
		@NotEmpty(message = "At least one category is required")
		List<@Valid CategoryDTO> categories) {

	public static ProductDTO from(Product product) {
		return new ProductDTO(
				product.getProductId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getImageUrl(),
				product.getCategories().stream().map(category -> CategoryDTO.from(category)).toList());
	}

}
