
package com.example.similarproducts.application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.similarproducts.application.ports.driven.ProductRepositoryPort;
import com.example.similarproducts.application.ports.driving.GetSimilarProductsServicePort;
import com.example.similarproducts.domain.ProductDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetSimilarProductsService implements GetSimilarProductsServicePort {

	private static final int MAX_CONCURRENCY = 10;

	private final ProductRepositoryPort productRepositoryPort;

	@Override
	public Mono<List<ProductDTO>> getSimilarProducts(String productId) {
		return productRepositoryPort.getSimilarProductIds(productId).flatMapMany(Flux::fromIterable)
				.flatMapSequential(productRepositoryPort::getProduct, MAX_CONCURRENCY).collectList();
	}
}
