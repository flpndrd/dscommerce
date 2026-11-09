package com.devsuperior.dscommerce.order.dto;

import com.devsuperior.dscommerce.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ClientDTO(
		@JsonProperty("id")
		Long userId,
		String name) {

	public static ClientDTO from(User user) {
		return new ClientDTO(user.getUserId(), user.getName());
	}

}
