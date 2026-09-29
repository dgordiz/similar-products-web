package com.example.similarproducts.driving.controllers.mappers;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.similarproducts.domain.ProductDTO;
import com.example.similarproducts.driving.rest.generated.model.ProductDetail;

@Mapper(componentModel = "spring")
public interface SimilarProductsRestMapper {

	@Mapping(source = "price", target = "price")
	ProductDetail toResponse(ProductDTO product);

	default Double map(BigDecimal value) {
		return value != null ? value.doubleValue() : null;
	}

}
