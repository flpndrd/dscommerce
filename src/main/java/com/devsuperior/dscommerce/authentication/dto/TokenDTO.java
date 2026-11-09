package com.devsuperior.dscommerce.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenDTO(
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("token_type")
		String tokenType,
		@JsonProperty("expires_in")
		long expiresIn) {}
