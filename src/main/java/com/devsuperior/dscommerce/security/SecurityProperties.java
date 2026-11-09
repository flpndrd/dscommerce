package com.devsuperior.dscommerce.security;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties("security")
public record SecurityProperties(
		String clientId,
		String clientSecret,
		Jwt jwt) {

	public record Jwt(
			@DurationUnit(ChronoUnit.SECONDS)
			Duration duration) {}

}
