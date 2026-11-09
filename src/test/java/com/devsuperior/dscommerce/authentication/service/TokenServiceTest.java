package com.devsuperior.dscommerce.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.devsuperior.dscommerce.authentication.dto.TokenDTO;
import com.devsuperior.dscommerce.authentication.exception.UnsupportedGrantTypeException;
import com.devsuperior.dscommerce.security.SecurityProperties;
import com.devsuperior.dscommerce.user.UserFixture;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	private static final Instant NOW = Instant.parse("2026-10-31T12:00:00Z");

	private static final Duration TOKEN_DURATION = Duration.ofSeconds(86400);

	private static final String TOKEN_VALUE = "signed-token";

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtEncoder jwtEncoder;

	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		SecurityProperties securityProperties = new SecurityProperties(
				"myclientid",
				"myclientsecret",
				new SecurityProperties.Jwt(TOKEN_DURATION));

		tokenService = new TokenService(
				authenticationManager,
				jwtEncoder,
				securityProperties,
				Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Nested
	class Create {

		@Test
		void shouldReturnBearerTokenWithUserClaimsWhenCredentialsAreValid() {
			Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
					UserFixture.CLIENT_EMAIL,
					null,
					AuthorityUtils.createAuthorityList(UserFixture.CLIENT_AUTHORITY));
			given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
			given(jwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(aJwt());

			TokenDTO tokenDTO = tokenService.create("password", UserFixture.CLIENT_EMAIL, UserFixture.PASSWORD);

			ArgumentCaptor<JwtEncoderParameters> jwtEncoderParametersCaptor = ArgumentCaptor.forClass(
					JwtEncoderParameters.class);
			then(jwtEncoder).should().encode(jwtEncoderParametersCaptor.capture());
			JwtClaimsSet jwtClaimsSet = jwtEncoderParametersCaptor.getValue().getClaims();
			assertThat(tokenDTO.accessToken()).isEqualTo(TOKEN_VALUE);
			assertThat(tokenDTO.tokenType()).isEqualTo("Bearer");
			assertThat(tokenDTO.expiresIn()).isEqualTo(TOKEN_DURATION.toSeconds());
			assertThat(jwtClaimsSet.getClaimAsString("username")).isEqualTo(UserFixture.CLIENT_EMAIL);
			assertThat(jwtClaimsSet.<List<String>>getClaim("authorities"))
					.containsExactly(UserFixture.CLIENT_AUTHORITY);
			assertThat(jwtClaimsSet.getExpiresAt()).isEqualTo(NOW.plus(TOKEN_DURATION));
		}

		@Test
		void shouldThrowUnsupportedGrantTypeExceptionWhenGrantTypeIsNotPassword() {
			assertThatThrownBy(() -> tokenService.create(
					"client_credentials",
					UserFixture.CLIENT_EMAIL,
					UserFixture.PASSWORD))
					.isInstanceOf(UnsupportedGrantTypeException.class);
			then(authenticationManager).should(never()).authenticate(any(Authentication.class));
		}

	}

	private static Jwt aJwt() {
		return Jwt.withTokenValue(TOKEN_VALUE).header("alg", "RS256").subject(UserFixture.CLIENT_EMAIL).build();
	}

}
