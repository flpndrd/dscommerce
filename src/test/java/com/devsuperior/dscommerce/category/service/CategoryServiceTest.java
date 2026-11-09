package com.devsuperior.dscommerce.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devsuperior.dscommerce.category.dto.CategoryDTO;
import com.devsuperior.dscommerce.category.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private CategoryService categoryService;

	@Nested
	class ListCategories {

		@Test
		void shouldReturnAllCategories() {
			List<CategoryDTO> expectedCategories = List.of(
					new CategoryDTO(1L, "Livros"),
					new CategoryDTO(2L, "Eletrônicos"));
			given(categoryRepository.listAll()).willReturn(expectedCategories);

			List<CategoryDTO> categories = categoryService.list();

			assertThat(categories).isEqualTo(expectedCategories);
		}

	}

}
