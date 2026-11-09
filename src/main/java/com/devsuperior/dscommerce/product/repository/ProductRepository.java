package com.devsuperior.dscommerce.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.dscommerce.product.dto.ProductSummaryDTO;
import com.devsuperior.dscommerce.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("""
			SELECT new com.devsuperior.dscommerce.product.dto.ProductSummaryDTO(
				product.productId,
				product.name,
				product.price,
				product.imageUrl)
			FROM Product product
			WHERE UPPER(product.name) LIKE UPPER(CONCAT('%', :name, '%'))
			""")
	Page<ProductSummaryDTO> listByName(String name, Pageable pageable);

	@Query("""
			SELECT product
			FROM Product product
			LEFT JOIN FETCH product.categories
			WHERE product.productId = :productId
			""")
	Optional<Product> findWithCategoriesByProductId(Long productId);

	@Query("""
			SELECT COUNT(orderItem) > 0
			FROM OrderItem orderItem
			WHERE orderItem.orderItemId.product.productId = :productId
			""")
	boolean existsInOrders(Long productId);

}
