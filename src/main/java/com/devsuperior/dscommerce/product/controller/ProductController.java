package com.devsuperior.dscommerce.product.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.devsuperior.dscommerce.product.dto.ProductDTO;
import com.devsuperior.dscommerce.product.dto.ProductSummaryDTO;
import com.devsuperior.dscommerce.product.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/{productId}")
	ResponseEntity<ProductDTO> find(@PathVariable Long productId) {
		ProductDTO productDTO = productService.find(productId);
		return ResponseEntity.ok(productDTO);
	}

	@GetMapping
	ResponseEntity<Page<ProductSummaryDTO>> list(@RequestParam(defaultValue = "") String name, Pageable pageable) {
		Page<ProductSummaryDTO> productPage = productService.list(name, pageable);
		return ResponseEntity.ok(productPage);
	}

	@PostMapping
	ResponseEntity<ProductDTO> create(@Valid @RequestBody ProductDTO productDTO) {
		ProductDTO createdProductDTO = productService.create(productDTO);
		URI uri = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{productId}")
				.buildAndExpand(createdProductDTO.productId())
				.toUri();

		return ResponseEntity.created(uri).body(createdProductDTO);
	}

	@PutMapping("/{productId}")
	ResponseEntity<ProductDTO> update(@PathVariable Long productId, @Valid @RequestBody ProductDTO productDTO) {
		ProductDTO updatedProductDTO = productService.update(productId, productDTO);
		return ResponseEntity.ok(updatedProductDTO);
	}

	@DeleteMapping("/{productId}")
	ResponseEntity<Void> delete(@PathVariable Long productId) {
		productService.delete(productId);
		return ResponseEntity.noContent().build();
	}

}
