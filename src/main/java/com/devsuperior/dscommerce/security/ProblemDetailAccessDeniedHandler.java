package com.devsuperior.dscommerce.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

	private final HandlerExceptionResolver handlerExceptionResolver;

	public ProblemDetailAccessDeniedHandler(HandlerExceptionResolver handlerExceptionResolver) {
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Override
	public void handle(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			AccessDeniedException accessDeniedException) {
		handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null, accessDeniedException);
	}

}
