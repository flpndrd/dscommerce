package com.devsuperior.dscommerce.category.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_category")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long categoryId;

	private String name;

	protected Category() {
	}

	public Category(String name) {
		this.name = name;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public String getName() {
		return name;
	}

}
