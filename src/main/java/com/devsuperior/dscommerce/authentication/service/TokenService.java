package com.devsuperior.dscommerce.authentication.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.devsuperior.dscommerce.authentication.dto.TokenDTO;
import com.devsuperior.dscommerce.authentication.exception.UnsupportedGrantTypeException;
import com.devsuperior.dscommerce.security.SecurityProperties;
import com.devsuperior.dscommerce.security.TokenClaims;

@Service
public class TokenService {

	private static final String PASSWORD_GRANT_TYPE = "password";

	private static final String BEARER_TOKEN_TYPE = "Bearer";

	private final AuthenticationManager authenticationManager;

	private final JwtEncoder jwtEncoder;

	private final SecurityProperties securityProperties;

	private final Clock clock;

	public TokenService(
			AuthenticationManager authenticationManager,
			JwtEncoder jwtEncoder,
			SecurityProperties securityProperties,
			Clock clock) {
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
		this.securityProperties = securityProperties;
		this.clock = clock;
	}

	public TokenDTO create(String grantType, String username, String password) {
		if (!PASSWORD_GRANT_TYPE.equals(grantType)) {
			throw new UnsupportedGrantTypeException();
		}
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(username, password));

		List<String> authorities = authentication
				.getAuthorities()
				.stream()
				.map(grantedAuthority -> grantedAuthority.getAuthority())
				.toList();

		Instant issuedAt = clock.instant();
		Duration duration = securityProperties.jwt().duration();
		JwtClaimsSet jwtClaimsSet = JwtClaimsSet
				.builder()
				.subject(authentication.getName())
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plus(duration))
				.claim(TokenClaims.USERNAME, authentication.getName())
				.claim(TokenClaims.AUTHORITIES, authorities)
				.build();

		JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();
		String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet)).getTokenValue();
		return new TokenDTO(accessToken, BEARER_TOKEN_TYPE, duration.toSeconds());
	}

}
