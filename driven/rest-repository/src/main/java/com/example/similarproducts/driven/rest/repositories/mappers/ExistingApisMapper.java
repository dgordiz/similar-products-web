package com.example.similarproducts.driven.rest.repositories.mappers;

import org.mapstruct.Mapper;

import com.example.existingapis.generated.model.ProductDetail;
import com.example.similarproducts.domain.ProductDTO;

@Mapper(componentModel = "spring")
public interface ExistingApisMapper {

	ProductDTO toProductDTO(ProductDetail productDetail);
}