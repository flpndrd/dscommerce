package com.devsuperior.dscommerce.category.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.dscommerce.category.dto.CategoryDTO;
import com.devsuperior.dscommerce.category.service.CategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	ResponseEntity<List<CategoryDTO>> list() {
		List<CategoryDTO> categories = categoryService.list();
		return ResponseEntity.ok(categories);
	}

}
