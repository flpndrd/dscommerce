package com.devsuperior.dscommerce.order.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.devsuperior.dscommerce.order.dto.OrderDTO;
import com.devsuperior.dscommerce.order.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/{orderId}")
	ResponseEntity<OrderDTO> find(@PathVariable Long orderId) {
		OrderDTO orderDTO = orderService.find(orderId);
		return ResponseEntity.ok(orderDTO);
	}

	@PostMapping
	ResponseEntity<OrderDTO> create(@Valid @RequestBody OrderDTO orderDTO) {
		OrderDTO createdOrderDTO = orderService.create(orderDTO);
		URI uri = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{orderId}")
				.buildAndExpand(createdOrderDTO.orderId())
				.toUri();

		return ResponseEntity.created(uri).body(createdOrderDTO);
	}

}
