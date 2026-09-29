
package com.example.similarproducts.application.services;

import java.util.Set;

import com.example.similarproducts.application.ports.driven.ProductRepositoryPort;
import com.example.similarproducts.application.ports.driving.GetSimilarProductsServicePort;
import com.example.similarproducts.domain.ProductDTO;

public class GetSimilarProductsService implements GetSimilarProductsServicePort {

	private final ProductRepositoryPort productRepositoryPort;

	public GetSimilarProductsService(ProductRepositoryPort productRepositoryPort) {
		this.productRepositoryPort = productRepositoryPort;
	}

	@Override
	public Set<ProductDTO> getSimilarProducts(String productId) {
		return productRepositoryPort.getSimilarProducts(productId);
	}

}
