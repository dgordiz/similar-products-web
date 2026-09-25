package com.example.similarproducts.driving.controllers.adapters;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.similarproducts.application.ports.driving.GetSimilarProductsServicePort;
import com.example.similarproducts.driving.controllers.responses.ProductResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class SimilarProductsController {

	private final GetSimilarProductsServicePort useCase;

	@GetMapping("/{productId}/similar")
	public Mono<List<ProductResponse>> getSimilarProducts(@PathVariable String productId) {

		return useCase.getSimilarProducts(productId)
				.map(products -> products.stream().map(ProductResponse::from).toList());
	}
}
