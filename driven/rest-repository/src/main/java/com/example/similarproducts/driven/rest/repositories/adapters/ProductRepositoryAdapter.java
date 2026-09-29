package com.example.similarproducts.driven.rest.repositories.adapters;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.similarproducts.application.exceptions.ErrorCode;
import com.example.similarproducts.application.exceptions.SimilarProductsException;
import com.example.similarproducts.application.ports.driven.ProductRepositoryPort;
import com.example.similarproducts.domain.ProductDTO;
import com.example.similarproducts.driven.rest.repositories.clients.ExistingApisClient;
import com.example.similarproducts.driven.rest.repositories.mappers.ExistingApisMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort{

	private static final Logger log = LoggerFactory.getLogger(ProductRepositoryAdapter.class);

	private static final int MAX_CONCURRENCY = 10;

	private final ExistingApisClient existingApisClient;
	private final ExistingApisMapper mapper;

	public Set<ProductDTO> getSimilarProducts(String productId) {

		log.info("Getting similar products for productId={}", productId);

		Set<ProductDTO> products = getSimilarProductIds(productId).flatMapMany(Flux::fromIterable)
				.flatMapSequential(this::getProduct, MAX_CONCURRENCY).collectList().map(LinkedHashSet::new).block();

		log.info("Similar products retrieved for productId={}, count={}", productId,
				products != null ? products.size() : 0);

		return products;
	}

	private Mono<Set<String>> getSimilarProductIds(String productId) {

		log.debug("Calling existing catalog for similar product ids, productId={}", productId);

		return existingApisClient.getProductSimilarIds(productId).map(set -> (Set<String>) new LinkedHashSet<>(set))
				.doOnSuccess(ids -> log.debug("Similar product ids retrieved, productId={}, count={}", productId,
						ids != null ? ids.size() : 0))
				.onErrorMap(mapException("Error getting similar product ids for product " + productId,
						"Timeout getting similar product ids for product " + productId));
	}

	private Mono<ProductDTO> getProduct(String productId) {

		log.debug("Calling existing catalog for productId={}", productId);

		return existingApisClient.getProductById(productId).map(mapper::toProductDTO)
				.doOnSuccess(product -> log.debug("Product retrieved successfully, productId={}", productId))
				.doOnError(error -> log.warn("Failed to retrieve product, productId={}, error={}", productId,
						error.getMessage()))
				.onErrorMap(mapException("Error getting product " + productId, "Timeout getting product " + productId))
				.onErrorResume(error -> {
					log.warn("Skipping productId={} due to error={}", productId, error.getMessage());

					return Mono.empty();
				});
	}

	private Function<Throwable, Throwable> mapException(String errorMessage, String timeoutMessage) {

		return exception -> {

			if (exception instanceof SimilarProductsException) {
				return exception;
			}

			if (exception instanceof TimeoutException) {

				log.warn("Timeout calling existing catalog. error={}", timeoutMessage);

				return new SimilarProductsException(timeoutMessage, ErrorCode.GATEWAY_TIMEOUT);
			}

			if (exception instanceof WebClientResponseException responseException) {

				if (responseException.getStatusCode().value() == 404) {

					log.warn("Product not found in existing catalog. status={}, message={}",
							responseException.getStatusCode().value(), errorMessage);

					return new SimilarProductsException("Product not found", ErrorCode.NOT_FOUND);
				}

				if (responseException.getStatusCode().value() == 504) {

					log.warn("Gateway timeout from existing catalog. message={}", timeoutMessage);

					return new SimilarProductsException(timeoutMessage, ErrorCode.GATEWAY_TIMEOUT);
				}

				log.error("Existing catalog returned HTTP error. status={}, message={}",
						responseException.getStatusCode().value(), errorMessage);

			} else {

				log.error("Error calling existing catalog. message={}", errorMessage, exception);
			}

			return new SimilarProductsException(errorMessage, ErrorCode.BAD_GATEWAY);
		};
	}
}
