package com.devsuperior.dscommerce.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_role")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long roleId;

	private String authority;

	protected Role() {
	}

	public Role(String authority) {
		this.authority = authority;
	}

	public String getAuthority() {
		return authority;
	}

}
