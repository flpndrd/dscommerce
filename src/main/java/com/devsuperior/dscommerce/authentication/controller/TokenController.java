package com.devsuperior.dscommerce.authentication.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.dscommerce.authentication.dto.TokenDTO;
import com.devsuperior.dscommerce.authentication.service.TokenService;

@RestController
public class TokenController {

	private final TokenService tokenService;

	public TokenController(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	@PostMapping(path = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	ResponseEntity<TokenDTO> create(
			@RequestParam(name = "grant_type") String grantType,
			@RequestParam String username,
			@RequestParam String password) {
		TokenDTO tokenDTO = tokenService.create(grantType, username, password);
		return ResponseEntity.ok(tokenDTO);
	}

}
