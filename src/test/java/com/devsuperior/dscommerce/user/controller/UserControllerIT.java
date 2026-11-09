package com.devsuperior.dscommerce.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.devsuperior.dscommerce.accesstoken.AccessTokenSupport;
import com.devsuperior.dscommerce.security.SecurityProperties;
import com.devsuperior.dscommerce.user.UserFixture;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {

	private final MockMvcTester mockMvcTester;

	private final AccessTokenSupport accessTokenSupport;

	@Autowired
	UserControllerIT(MockMvcTester mockMvcTester, SecurityProperties securityProperties) {
		this.mockMvcTester = mockMvcTester;
		this.accessTokenSupport = new AccessTokenSupport(mockMvcTester, securityProperties);
	}

	@Nested
	class FindCurrentUser {

		@Test
		void shouldReturnLoggedUserWhenTokenIsValid() {
			String accessToken = accessTokenSupport.obtainAccessToken(UserFixture.CLIENT_EMAIL, UserFixture.PASSWORD);

			MvcTestResult mvcTestResult = mockMvcTester
					.get()
					.uri("/users/me")
					.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
					.exchange();

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult).bodyJson().extractingPath("$.email").isEqualTo(UserFixture.CLIENT_EMAIL);
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$.roles")
					.asArray()
					.containsExactly(UserFixture.CLIENT_AUTHORITY);
		}

		@Test
		void shouldReturnUnauthorizedWithBearerChallengeWhenTokenIsMissing() {
			MvcTestResult mvcTestResult = mockMvcTester.get().uri("/users/me").exchange();

			assertThat(mvcTestResult).hasStatus(HttpStatus.UNAUTHORIZED);
			assertThat(mvcTestResult.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer");
			assertThat(mvcTestResult).bodyJson().extractingPath("$.status").isEqualTo(401);
		}

	}

}
