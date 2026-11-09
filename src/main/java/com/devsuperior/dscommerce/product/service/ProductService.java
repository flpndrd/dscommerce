package com.devsuperior.dscommerce.product.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.category.dto.CategoryDTO;
import com.devsuperior.dscommerce.category.entity.Category;
import com.devsuperior.dscommerce.category.repository.CategoryRepository;
import com.devsuperior.dscommerce.product.dto.ProductDTO;
import com.devsuperior.dscommerce.product.dto.ProductSummaryDTO;
import com.devsuperior.dscommerce.product.entity.Product;
import com.devsuperior.dscommerce.product.exception.InvalidProductCategoryException;
import com.devsuperior.dscommerce.product.exception.ProductInUseException;
import com.devsuperior.dscommerce.product.exception.ProductNotFoundException;
import com.devsuperior.dscommerce.product.repository.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	private final CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
	}

	@Transactional(readOnly = true)
	public ProductDTO find(Long productId) {
		Product product = productRepository
				.findWithCategoriesByProductId(productId)
				.orElseThrow(() -> new ProductNotFoundException());

		return ProductDTO.from(product);
	}

	@Transactional(readOnly = true)
	public Page<ProductSummaryDTO> list(String name, Pageable pageable) {
		return productRepository.listByName(name, pageable);
	}

	@Transactional
	public ProductDTO create(ProductDTO productDTO) {
		Product product = new Product(
				productDTO.name(),
				productDTO.description(),
				productDTO.price(),
				productDTO.imageUrl());

		product.getCategories().addAll(findCategories(productDTO.categories()));
		Product savedProduct = productRepository.save(product);
		return ProductDTO.from(savedProduct);
	}

	@Transactional
	public ProductDTO update(Long productId, ProductDTO productDTO) {
		Product product = productRepository
				.findWithCategoriesByProductId(productId)
				.orElseThrow(() -> new ProductNotFoundException());

		product.setName(productDTO.name());
		product.setDescription(productDTO.description());
		product.setPrice(productDTO.price());
		product.setImageUrl(productDTO.imageUrl());
		product.getCategories().clear();
		product.getCategories().addAll(findCategories(productDTO.categories()));
		return ProductDTO.from(product);
	}

	@Transactional
	public void delete(Long productId) {
		if (!productRepository.existsById(productId)) {
			throw new ProductNotFoundException();
		}

		if (productRepository.existsInOrders(productId)) {
			throw new ProductInUseException();
		}
		productRepository.deleteById(productId);
	}

	private List<Category> findCategories(List<CategoryDTO> categoryDTOs) {
		Set<Long> categoryIds = categoryDTOs
				.stream()
				.map(categoryDTO -> categoryDTO.categoryId())
				.collect(Collectors.toSet());

		List<Category> categories = categoryRepository.findAllById(categoryIds);

		if (categories.size() != categoryIds.size()) {
			throw new InvalidProductCategoryException();
		}
		return categories;
	}

}
