package com.example.similarproducts.driving.controllers.responses;

import java.math.BigDecimal;

import com.example.similarproducts.domain.ProductDTO;

import lombok.Data;

@Data
public class ProductResponse {

	private String id;
	private String name;
	private BigDecimal price;
	private Boolean availability;

	public static ProductResponse from(ProductDTO product) {
		ProductResponse response = new ProductResponse();

		response.setId(product.getId());
		response.setName(product.getName());
		response.setPrice(product.getPrice());
		response.setAvailability(product.getAvailability());

		return response;
	}
}
