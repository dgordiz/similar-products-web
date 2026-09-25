package com.example.similarproducts.application.ports.driven;

import java.util.List;

import com.example.similarproducts.domain.ProductDTO;

import reactor.core.publisher.Mono;

public interface ProductRepositoryPort  {

	Mono<List<String>> getSimilarProductIds(String productId);

	Mono<ProductDTO> getProduct(String productId);
}
