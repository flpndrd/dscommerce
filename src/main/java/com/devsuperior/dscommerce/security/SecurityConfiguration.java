package com.devsuperior.dscommerce.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableConfigurationProperties({ SecurityProperties.class, CorsProperties.class })
public class SecurityConfiguration {

	private static final String TOKEN_PATH = "/oauth2/token";

	private static final String CLIENT_APPLICATION_AUTHORITY = "CLIENT_APPLICATION";

	private static final String BASIC_CHALLENGE = "Basic realm=\"dscommerce\"";

	private final SecurityProperties securityProperties;

	private final CorsProperties corsProperties;

	private final HandlerExceptionResolver handlerExceptionResolver;

	public SecurityConfiguration(
			SecurityProperties securityProperties,
			CorsProperties corsProperties,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
		this.securityProperties = securityProperties;
		this.corsProperties = corsProperties;
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Bean
	@Order(1)
	SecurityFilterChain tokenSecurityFilterChain(HttpSecurity httpSecurity, PasswordEncoder passwordEncoder) {
		ProblemDetailAuthenticationEntryPoint basicEntryPoint = new ProblemDetailAuthenticationEntryPoint(
				(_, httpServletResponse, _) -> httpServletResponse.setHeader(
						HttpHeaders.WWW_AUTHENTICATE,
						BASIC_CHALLENGE),
				handlerExceptionResolver);

		httpSecurity
				.securityMatcher(TOKEN_PATH)
				.csrf(csrfConfigurer -> csrfConfigurer.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationManager(clientAuthenticationManager(passwordEncoder))
				.httpBasic(httpBasicConfigurer -> httpBasicConfigurer.authenticationEntryPoint(basicEntryPoint))
				.exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
						.authenticationEntryPoint(basicEntryPoint))
				.authorizeHttpRequests(
						authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
								.anyRequest()
								.authenticated());

		return httpSecurity.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain resourceSecurityFilterChain(HttpSecurity httpSecurity) {
		ProblemDetailAuthenticationEntryPoint bearerEntryPoint = new ProblemDetailAuthenticationEntryPoint(
				new BearerTokenAuthenticationEntryPoint(),
				handlerExceptionResolver);

		ProblemDetailAccessDeniedHandler problemDetailAccessDeniedHandler = new ProblemDetailAccessDeniedHandler(
				handlerExceptionResolver);

		httpSecurity
				.csrf(csrfConfigurer -> csrfConfigurer.disable())
				.cors(Customizer.withDefaults())
				.headers(headersConfigurer -> headersConfigurer
						.frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin()))
				.sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.oauth2ResourceServer(oauth2ResourceServerConfigurer -> oauth2ResourceServerConfigurer
						.jwt(Customizer.withDefaults())
						.authenticationEntryPoint(bearerEntryPoint)
						.accessDeniedHandler(problemDetailAccessDeniedHandler))
				.exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
						.authenticationEntryPoint(bearerEntryPoint)
						.accessDeniedHandler(problemDetailAccessDeniedHandler))
				.authorizeHttpRequests(
						authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
								.requestMatchers(PathRequest.toH2Console())
								.permitAll()
								.requestMatchers(HttpMethod.GET, "/products", "/products/*", "/categories")
								.permitAll()
								.requestMatchers(HttpMethod.POST, "/products")
								.hasRole(Roles.ADMIN)
								.requestMatchers(HttpMethod.PUT, "/products/*")
								.hasRole(Roles.ADMIN)
								.requestMatchers(HttpMethod.DELETE, "/products/*")
								.hasRole(Roles.ADMIN)
								.requestMatchers(HttpMethod.GET, "/orders/*", "/users/me")
								.hasAnyRole(Roles.ADMIN, Roles.CLIENT)
								.requestMatchers(HttpMethod.POST, "/orders")
								.hasRole(Roles.CLIENT)
								.anyRequest()
								.authenticated());

		return httpSecurity.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(
			UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(daoAuthenticationProvider);
	}

	@Bean
	KeyPair jwtKeyPair() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			return keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			throw new IllegalStateException(noSuchAlgorithmException);
		}
	}

	@Bean
	JwtEncoder jwtEncoder(KeyPair jwtKeyPair) {
		return NimbusJwtEncoder
				.withKeyPair((RSAPublicKey) jwtKeyPair.getPublic(), (RSAPrivateKey) jwtKeyPair.getPrivate())
				.build();
	}

	@Bean
	JwtDecoder jwtDecoder(KeyPair jwtKeyPair) {
		return NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic()).build();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(TokenClaims.AUTHORITIES);
		jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedOrigins(corsProperties.origins());
		corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
		corsConfiguration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
		UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
		urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
		return urlBasedCorsConfigurationSource;
	}

	private AuthenticationManager clientAuthenticationManager(PasswordEncoder passwordEncoder) {
		UserDetails clientUserDetails = User
				.withUsername(securityProperties.clientId())
				.password(passwordEncoder.encode(securityProperties.clientSecret()))
				.authorities(CLIENT_APPLICATION_AUTHORITY)
				.build();

		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(
				new InMemoryUserDetailsManager(clientUserDetails));

		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(daoAuthenticationProvider);
	}

}
