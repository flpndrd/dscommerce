package com.devsuperior.dscommerce.product.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductSummaryDTO(
		@JsonProperty("id")
		Long productId,
		String name,
		BigDecimal price,
		@JsonProperty("imgUrl")
		String imageUrl) {}
