package com.example.similarproducts.driven.rest.repositories.clients;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.existingapis.generated.api.DefaultApi;
import com.example.existingapis.generated.model.ProductDetail;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import reactor.core.publisher.Mono;

@Component
public class ExistingApisClient extends DefaultApi {

	public ExistingApisClient(@Value("${existing-api.base-url}") String baseUrl) {
		super.getApiClient().setBasePath(baseUrl);
	}

	@Override
	@Bulkhead(name = "existing-catalog")
	public Mono<Set<String>> getProductSimilarIds(String productId) {
		return super.getProductSimilarIds(productId);
	}

	@Override
	@Bulkhead(name = "existing-catalog")
	public Mono<ProductDetail> getProductById(String productId) {
		return super.getProductById(productId);
	}
}
