package com.devsuperior.dscommerce.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.dscommerce.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("""
			SELECT user
			FROM User user
			LEFT JOIN FETCH user.roles
			WHERE user.email = :email
			""")
	Optional<User> findWithRolesByEmail(String email);

	Optional<User> findByEmail(String email);

}
