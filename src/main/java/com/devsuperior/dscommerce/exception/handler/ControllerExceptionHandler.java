package com.devsuperior.dscommerce.exception.handler;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.devsuperior.dscommerce.exception.BusinessRuleException;
import com.devsuperior.dscommerce.exception.InvalidRequestException;
import com.devsuperior.dscommerce.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

	private static final String UNEXPECTED_ERROR_TITLE = "Internal error";

	private static final String UNEXPECTED_ERROR_MESSAGE = "The operation could not be completed. Report the identifier %s to support.";

	private static final String INVALID_REQUEST_TITLE = "Invalid request";

	private static final String RESOURCE_NOT_FOUND_TITLE = "Resource not found";

	private static final String BUSINESS_RULE_TITLE = "Conflict";

	private static final String AUTHENTICATION_MESSAGE = "Authentication is required or the credentials are invalid.";

	private static final String ACCESS_DENIED_MESSAGE = "Access denied.";

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException methodArgumentNotValidException,
			HttpHeaders httpHeaders,
			HttpStatusCode httpStatusCode,
			WebRequest webRequest) {
		ProblemDetail problemDetail = methodArgumentNotValidException.updateAndGetBody(
				getMessageSource(),
				LocaleContextHolder.getLocale());

		List<FieldErrorResponse> fieldErrors = methodArgumentNotValidException
				.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(fieldError -> new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
				.toList();

		problemDetail.setStatus(HttpStatus.UNPROCESSABLE_CONTENT);
		problemDetail.setTitle(HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase());
		problemDetail.setProperty("errors", fieldErrors);
		return handleExceptionInternal(
				methodArgumentNotValidException,
				problemDetail,
				httpHeaders,
				HttpStatus.UNPROCESSABLE_CONTENT,
				webRequest);
	}

	@ExceptionHandler(InvalidRequestException.class)
	ProblemDetail handleInvalidRequest(InvalidRequestException invalidRequestException) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				invalidRequestException.getMessage());

		problemDetail.setTitle(INVALID_REQUEST_TITLE);
		return problemDetail;
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handleResourceNotFound(ResourceNotFoundException resourceNotFoundException) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				resourceNotFoundException.getMessage());

		problemDetail.setTitle(RESOURCE_NOT_FOUND_TITLE);
		return problemDetail;
	}

	@ExceptionHandler(BusinessRuleException.class)
	ProblemDetail handleBusinessRule(BusinessRuleException businessRuleException) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				businessRuleException.getMessage());

		problemDetail.setTitle(BUSINESS_RULE_TITLE);
		return problemDetail;
	}

	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail handleAuthentication(AuthenticationException authenticationException) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, AUTHENTICATION_MESSAGE);
		problemDetail.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());
		return problemDetail;
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail handleAccessDenied(AccessDeniedException accessDeniedException) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ACCESS_DENIED_MESSAGE);
		problemDetail.setTitle(HttpStatus.FORBIDDEN.getReasonPhrase());
		return problemDetail;
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpectedError(Exception exception, HttpServletRequest httpServletRequest) {
		String errorIdentifier = UUID.randomUUID().toString();
		LOGGER.error(
				"Unexpected error {} on {} {}",
				errorIdentifier,
				httpServletRequest.getMethod(),
				httpServletRequest.getRequestURI(),
				exception);

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				UNEXPECTED_ERROR_MESSAGE.formatted(errorIdentifier));

		problemDetail.setTitle(UNEXPECTED_ERROR_TITLE);
		problemDetail.setProperty("errorIdentifier", errorIdentifier);
		return problemDetail;
	}

	public record FieldErrorResponse(
			String fieldName,
			String message) {}

}
