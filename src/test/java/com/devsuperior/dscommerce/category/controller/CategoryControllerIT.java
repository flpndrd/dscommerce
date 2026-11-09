package com.devsuperior.dscommerce.category.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerIT {

	private final MockMvcTester mockMvcTester;

	@Autowired
	CategoryControllerIT(MockMvcTester mockMvcTester) {
		this.mockMvcTester = mockMvcTester;
	}

	@Nested
	class ListCategories {

		@Test
		void shouldReturnAllCategoriesWhenTokenIsMissing() {
			MvcTestResult mvcTestResult = mockMvcTester.get().uri("/categories").exchange();

			assertThat(mvcTestResult).hasStatusOk();
			assertThat(mvcTestResult)
					.bodyJson()
					.extractingPath("$[*].name")
					.asArray()
					.containsExactly("Livros", "Eletrônicos", "Computadores");
		}

	}

}
