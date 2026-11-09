package com.devsuperior.dscommerce.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

class ControllerExceptionHandlerTest {

	private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();

	@Nested
	class HandleUnexpectedError {

		@Test
		void shouldReturnInternalServerErrorWithErrorIdentifierWithoutTechnicalDetail() {
			MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest("GET", "/products");

			ProblemDetail problemDetail = controllerExceptionHandler.handleUnexpectedError(
					new IllegalStateException("Database connection lost"),
					mockHttpServletRequest);

			String errorIdentifier = (String) problemDetail.getProperties().get("errorIdentifier");
			assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
			assertThat(problemDetail.getDetail()).contains(errorIdentifier).doesNotContain("Database connection lost");
		}

	}

}
