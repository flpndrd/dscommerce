package com.devsuperior.dscommerce.category.dto;

import com.devsuperior.dscommerce.category.entity.Category;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record CategoryDTO(
		@JsonProperty("id")
		@NotNull(message = "Category id is required")
		Long categoryId,
		String name) {

	public static CategoryDTO from(Category category) {
		return new CategoryDTO(category.getCategoryId(), category.getName());
	}

}
