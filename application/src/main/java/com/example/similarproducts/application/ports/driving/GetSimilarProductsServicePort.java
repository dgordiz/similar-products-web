package com.example.similarproducts.application.ports.driving;

import java.util.List;

import com.example.similarproducts.domain.ProductDTO;

import reactor.core.publisher.Mono;

public interface GetSimilarProductsServicePort {

	Mono<List<ProductDTO>> getSimilarProducts(String productId);
}