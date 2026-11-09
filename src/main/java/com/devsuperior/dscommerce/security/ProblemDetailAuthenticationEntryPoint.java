package com.devsuperior.dscommerce.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final AuthenticationEntryPoint headerEntryPoint;

	private final HandlerExceptionResolver handlerExceptionResolver;

	public ProblemDetailAuthenticationEntryPoint(
			AuthenticationEntryPoint headerEntryPoint,
			HandlerExceptionResolver handlerExceptionResolver) {
		this.headerEntryPoint = headerEntryPoint;
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Override
	public void commence(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			AuthenticationException authenticationException)
			throws IOException,
			ServletException {
		headerEntryPoint.commence(httpServletRequest, httpServletResponse, authenticationException);
		handlerExceptionResolver.resolveException(
				httpServletRequest,
				httpServletResponse,
				null,
				authenticationException);
	}

}
