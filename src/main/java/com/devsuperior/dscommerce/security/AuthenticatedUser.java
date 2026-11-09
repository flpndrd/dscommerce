package com.devsuperior.dscommerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {

	private static final String ADMIN_AUTHORITY = "ROLE_" + Roles.ADMIN;

	public String getEmail() {
		Jwt jwt = (Jwt) authentication().getPrincipal();
		return jwt.getClaimAsString(TokenClaims.USERNAME);
	}

	public boolean isAdmin() {
		return authentication()
				.getAuthorities()
				.stream()
				.anyMatch(grantedAuthority -> ADMIN_AUTHORITY.equals(grantedAuthority.getAuthority()));
	}

	private Authentication authentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

}
