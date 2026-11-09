package com.devsuperior.dscommerce.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.devsuperior.dscommerce.user.UserFixture;

class AuthenticatedUserTest {

	private final AuthenticatedUser authenticatedUser = new AuthenticatedUser();

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Nested
	class GetEmail {

		@Test
		void shouldReturnUsernameClaimOfToken() {
			authenticateWith(UserFixture.CLIENT_EMAIL, UserFixture.CLIENT_AUTHORITY);

			String email = authenticatedUser.getEmail();

			assertThat(email).isEqualTo(UserFixture.CLIENT_EMAIL);
		}

	}

	@Nested
	class IsAdmin {

		@Test
		void shouldReturnTrueWhenUserHasAdminAuthority() {
			authenticateWith(UserFixture.ADMIN_EMAIL, UserFixture.CLIENT_AUTHORITY, UserFixture.ADMIN_AUTHORITY);

			boolean admin = authenticatedUser.isAdmin();

			assertThat(admin).isTrue();
		}

		@Test
		void shouldReturnFalseWhenUserHasOnlyClientAuthority() {
			authenticateWith(UserFixture.CLIENT_EMAIL, UserFixture.CLIENT_AUTHORITY);

			boolean admin = authenticatedUser.isAdmin();

			assertThat(admin).isFalse();
		}

	}

	private static void authenticateWith(String email, String... authorities) {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").claim("username", email).build();
		SecurityContextHolder
				.getContext()
				.setAuthentication(new JwtAuthenticationToken(jwt, AuthorityUtils.createAuthorityList(authorities)));
	}

}
