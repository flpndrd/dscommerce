package com.devsuperior.dscommerce.accesstoken;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.devsuperior.dscommerce.security.SecurityProperties;
import com.jayway.jsonpath.JsonPath;

public class AccessTokenSupport {

	private final MockMvcTester mockMvcTester;

	private final SecurityProperties securityProperties;

	public AccessTokenSupport(MockMvcTester mockMvcTester, SecurityProperties securityProperties) {
		this.mockMvcTester = mockMvcTester;
		this.securityProperties = securityProperties;
	}

	public MvcTestResult requestToken(String clientSecret, String grantType, String username, String password) {
		return mockMvcTester
				.post()
				.uri("/oauth2/token")
				.headers(httpHeaders -> httpHeaders.setBasicAuth(securityProperties.clientId(), clientSecret))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("grant_type", grantType)
				.param("username", username)
				.param("password", password)
				.exchange();
	}

	public String obtainAccessToken(String username, String password) {
		MvcTestResult mvcTestResult = requestToken(securityProperties.clientSecret(), "password", username, password);
		String responseBody = new String(mvcTestResult.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
		return JsonPath.read(responseBody, "$.access_token");
	}

}
