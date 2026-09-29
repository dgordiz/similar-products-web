package com.example.similarproducts.application.ports.driven;

import java.util.Set;

import com.example.similarproducts.domain.ProductDTO;

public interface ProductRepositoryPort {

	Set<ProductDTO> getSimilarProducts(String productId);
}
