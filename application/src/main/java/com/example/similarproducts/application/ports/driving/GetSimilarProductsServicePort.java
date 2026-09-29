package com.example.similarproducts.application.ports.driving;

import java.util.Set;

import com.example.similarproducts.domain.ProductDTO;

public interface GetSimilarProductsServicePort {

	Set<ProductDTO> getSimilarProducts(String productId);

}