package com.example.similarproducts.driving.controllers.adapters;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.similarproducts.application.ports.driving.GetSimilarProductsServicePort;
import com.example.similarproducts.driving.controllers.mappers.SimilarProductsRestMapper;
import com.example.similarproducts.driving.rest.generated.api.ProductApi;
import com.example.similarproducts.driving.rest.generated.model.ProductDetail;

@RestController
public class SimilarProductsController implements ProductApi {

	private final GetSimilarProductsServicePort service;
	private final SimilarProductsRestMapper mapper;

	public SimilarProductsController(GetSimilarProductsServicePort service, SimilarProductsRestMapper mapper) {
		this.service = service;
		this.mapper = mapper;
	}

	@Override
	public ResponseEntity<Set<ProductDetail>> getProductSimilar(String productId) {

		Set<ProductDetail> products = service.getSimilarProducts(productId).stream().map(mapper::toResponse)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		return ResponseEntity.ok(products);
	}

}
