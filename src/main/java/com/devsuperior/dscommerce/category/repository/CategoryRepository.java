package com.devsuperior.dscommerce.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.dscommerce.category.dto.CategoryDTO;
import com.devsuperior.dscommerce.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("""
			SELECT new com.devsuperior.dscommerce.category.dto.CategoryDTO(category.categoryId, category.name)
			FROM Category category
			ORDER BY category.categoryId
			""")
	List<CategoryDTO> listAll();

}
