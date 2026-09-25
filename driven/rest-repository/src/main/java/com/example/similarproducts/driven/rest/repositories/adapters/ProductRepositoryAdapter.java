package com.example.similarproducts.driven.rest.repositories.adapters;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.similarproducts.application.exceptions.SimilarProductsException;
import com.example.similarproducts.application.ports.driven.ProductRepositoryPort;
import com.example.similarproducts.domain.ProductDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

	private final WebClient webClient;

	@Value("${existing-api.base-url}")
	private String baseUrl;

	
	@Override
	public Mono<List<String>> getSimilarProductIds(String productId) {
	    return webClient.get()
	            .uri(baseUrl + "/product/{productId}/similarids", productId)
	            .retrieve()
	            .onStatus(
	                    status -> status.value() == 404,
	                    response -> Mono.error(
	                            new SimilarProductsException(
	                                    "Product not found: " + productId,
	                                    HttpStatus.NOT_FOUND
	                            )
	                    )
	            )
	            .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
	            .onErrorMap(exception -> {

	                if (exception instanceof SimilarProductsException) {
	                    return exception;
	                }

	                return new SimilarProductsException(
	                        "Error getting similar product ids for product " + productId,
	                        HttpStatus.BAD_GATEWAY
	                );
	            });
	}
	
	@Override
	public Mono<ProductDTO> getProduct(String productId) {
		return webClient.get().uri(baseUrl + "/product/{productId}", productId).retrieve()
				.onStatus(status -> status.value() == 404,
						response -> Mono.error(new SimilarProductsException("Product not found: " + productId,
								HttpStatus.NOT_FOUND)))
				.bodyToMono(ProductDTO.class).onErrorMap(exception -> {
					if (exception instanceof SimilarProductsException) {
						return exception;
					}

					return new SimilarProductsException("Error getting product " + productId,
							HttpStatus.BAD_GATEWAY);
				});
	}
}