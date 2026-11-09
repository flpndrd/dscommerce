package com.devsuperior.dscommerce.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.dscommerce.user.dto.UserDTO;
import com.devsuperior.dscommerce.user.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	ResponseEntity<UserDTO> findCurrentUser() {
		UserDTO userDTO = userService.findCurrentUser();
		return ResponseEntity.ok(userDTO);
	}

}
