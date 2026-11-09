package com.devsuperior.dscommerce.authentication.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.devsuperior.dscommerce.accesstoken.AccessTokenSupport;
import com.devsuperior.dscommerce.security.SecurityProperties;
import com.devsuperior.dscommerce.user.UserFixture;

@SpringBootTest
@AutoConfigureMockMvc
class TokenControllerIT {

	private final MockMvcTester mockMvcTester;

	private final SecurityProperties securityProperties;

	private final AccessTokenSupport accessTokenSupport;

	@Autowired
	TokenControllerIT(MockMvcTester mockMvcTester, SecurityProperties securityProperties) {
		this.mockMvcTester = mockMvcTester;
		this.securityProperties = securityProperties;
		this.accessTokenSupport = new AccessTokenSupport(mockMvcTester, securityProperties);
	}

	@Nested
	class Create {

		@Test
		void shouldReturnBearerTokenWhenCredentialsAreValid() {
			MvcTestResult mvcTestResult = accessTokenSupport.requestToken(
					securityProperties.clientSecret(),
					"password",
					UserFixture.CLIENT_EMAIL,
					UserFixture.PASSWORD);

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.access_token").asString().isNotBlank();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.token_type").isEqualTo("Bearer");
			assertThat(mvcTestResult).bodyJson().extractingPath("$.expires_in").isEqualTo(86400);
		}

		@Test
		void shouldReturnUnauthorizedWhenPasswordIsWrong() {
			MvcTestResult mvcTestResult = accessTokenSupport.requestToken(
					securityProperties.clientSecret(),
					"password",
					UserFixture.CLIENT_EMAIL,
					"wrong-password");

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNAUTHORIZED);
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.detail")
					.isEqualTo("Authentication is required or the credentials are invalid.");
		}

		@Test
		void shouldReturnUnauthorizedWhenUserDoesNotExist() {
			MvcTestResult mvcTestResult = accessTokenSupport.requestToken(
					securityProperties.clientSecret(),
					"password",
					"nobody@gmail.com",
					UserFixture.PASSWORD);

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNAUTHORIZED);
		}

		@Test
		void shouldReturnUnauthorizedWithBasicChallengeWhenClientSecretIsWrong() {
			MvcTestResult mvcTestResult = accessTokenSupport.requestToken(
					"wrong-secret",
					"password",
					UserFixture.CLIENT_EMAIL,
					UserFixture.PASSWORD);

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNAUTHORIZED);
			assertThat(mvcTestResult).headers().hasValue(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"dscommerce\"");
			assertThat(mvcTestResult).bodyJson().extractingPath("$.status").isEqualTo(401);
		}

		@Test
		void shouldReturnBadRequestWhenGrantTypeIsNotPassword() {
			MvcTestResult mvcTestResult = accessTokenSupport.requestToken(
					securityProperties.clientSecret(),
					"client_credentials",
					UserFixture.CLIENT_EMAIL,
					UserFixture.PASSWORD);

			assertThat(mvcTestResult).hasStatus(HttpStatus.BAD_REQUEST);
			assertThat(mvcTestResult).bodyJson().extractingPath("$.detail").isEqualTo("Grant type must be password.");
		}

		@Test
		void shouldReturnBadRequestWhenUsernameIsMissing() {
			MvcTestResult mvcTestResult = mockMvcTester
					.post()
					.uri("/oauth2/token")
					.headers(httpHeaders -> httpHeaders.setBasicAuth(
							securityProperties.clientId(),
							securityProperties.clientSecret()))
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("grant_type", "password")
					.param("password", UserFixture.PASSWORD)
					.exchange();

			assertThat(mvcTestResult).hasStatus(HttpStatus.BAD_REQUEST);
		}

	}

}
