package com.devsuperior.dscommerce.user.dto;

import java.time.LocalDate;
import java.util.List;

import com.devsuperior.dscommerce.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDTO(
		@JsonProperty("id")
		Long userId,
		String name,
		String email,
		String phone,
		LocalDate birthDate,
		List<String> roles) {

	public static UserDTO from(User user) {
		return new UserDTO(
				user.getUserId(),
				user.getName(),
				user.getEmail(),
				user.getPhone(),
				user.getBirthDate(),
				user.getRoles().stream().map(role -> role.getAuthority()).toList());
	}

}
