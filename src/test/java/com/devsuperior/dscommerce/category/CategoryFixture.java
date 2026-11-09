package com.devsuperior.dscommerce.category;

import org.springframework.test.util.ReflectionTestUtils;

import com.devsuperior.dscommerce.category.entity.Category;

public final class CategoryFixture {

	public static final Long BOOKS_CATEGORY_ID = 1L;

	public static final Long NONEXISTENT_CATEGORY_ID = 999L;

	private CategoryFixture() {
	}

	public static Category aBooksCategory() {
		Category category = new Category("Livros");
		ReflectionTestUtils.setField(category, "categoryId", BOOKS_CATEGORY_ID);
		return category;
	}

}
