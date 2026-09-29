package com.example.similarproducts.driving.controllers.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.similarproducts.application.ports.driving.GetSimilarProductsServicePort;
import com.example.similarproducts.domain.ProductDTO;
import com.example.similarproducts.driving.controllers.mappers.SimilarProductsRestMapper;
import com.example.similarproducts.driving.rest.generated.model.ProductDetail;

@ExtendWith(MockitoExtension.class)
class SimilarProductsControllerTest {

	@Mock
	private GetSimilarProductsServicePort service;

	@Mock
	private SimilarProductsRestMapper mapper;

	private SimilarProductsController controller;

	@BeforeEach
	void setUp() {
		controller = new SimilarProductsController(service, mapper);
	}

	@Test
	void shouldReturnSimilarProducts() {

		ProductDTO product = product("2", "Product 2", new BigDecimal("20.5"), true);

		ProductDetail productDetail = productDetail("2", "Product 2", 20.5, true);

		Set<ProductDTO> products = new LinkedHashSet<>();
		products.add(product);

		when(service.getSimilarProducts("1")).thenReturn(products);

		when(mapper.toResponse(product)).thenReturn(productDetail);

		ResponseEntity<Set<ProductDetail>> response = controller.getProductSimilar("1");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		assertThat(response.getBody()).containsExactly(productDetail);

		verify(service).getSimilarProducts("1");

		verify(mapper).toResponse(product);
	}

	@Test
	void shouldReturnSimilarProductsInSameOrder() {

		ProductDTO product1 = product("2", "Product 2", new BigDecimal("20.5"), true);

		ProductDTO product2 = product("3", "Product 3", new BigDecimal("30.5"), false);

		ProductDTO product3 = product("4", "Product 4", new BigDecimal("40.5"), true);

		ProductDetail productDetail1 = productDetail("2", "Product 2", 20.5, true);

		ProductDetail productDetail2 = productDetail("3", "Product 3", 30.5, false);

		ProductDetail productDetail3 = productDetail("4", "Product 4", 40.5, true);

		Set<ProductDTO> products = new LinkedHashSet<>();
		products.add(product1);
		products.add(product2);
		products.add(product3);

		when(service.getSimilarProducts("1")).thenReturn(products);

		when(mapper.toResponse(product1)).thenReturn(productDetail1);

		when(mapper.toResponse(product2)).thenReturn(productDetail2);

		when(mapper.toResponse(product3)).thenReturn(productDetail3);

		ResponseEntity<Set<ProductDetail>> response = controller.getProductSimilar("1");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		assertThat(response.getBody()).containsExactly(productDetail1, productDetail2, productDetail3);

		verify(service).getSimilarProducts("1");

		verify(mapper).toResponse(product1);

		verify(mapper).toResponse(product2);

		verify(mapper).toResponse(product3);
	}

	@Test
	void shouldReturnEmptySetWhenThereAreNoSimilarProducts() {

		when(service.getSimilarProducts("1")).thenReturn(new LinkedHashSet<>());

		ResponseEntity<Set<ProductDetail>> response = controller.getProductSimilar("1");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		assertThat(response.getBody()).isNotNull().isEmpty();

		verify(service).getSimilarProducts("1");
	}

	@Test
	void shouldReturnMappedProducts() {

	ProductDTO product1 =
	        product("2", "Product 2", new BigDecimal("20.5"), true);

	ProductDTO product2 =
	        product("3", "Product 3", new BigDecimal("30.5"), false);

	ProductDetail productDetail1 =
	        productDetail("2", "Product 2", 20.5, true);

	ProductDetail productDetail2 =
	        productDetail("3", "Product 3", 30.5, false);

	Set<ProductDTO> products = new LinkedHashSet<>();
	products.add(product1);
	products.add(product2);

	when(service.getSimilarProducts("1"))
	        .thenReturn(products);

	when(mapper.toResponse(product1))
	        .thenReturn(productDetail1);

	when(mapper.toResponse(product2))
	        .thenReturn(productDetail2);

	ResponseEntity<Set<ProductDetail>> response =
	        controller.getProductSimilar("1");

	assertThat(response.getBody())
	        .containsExactly(
	                productDetail1,
	                productDetail2
	        );

	verify(mapper).toResponse(product1);
	verify(mapper).toResponse(product2);


	}

	private ProductDTO product(String id, String name, BigDecimal price, boolean availability) {

		ProductDTO product = new ProductDTO();
		product.setId(id);
		product.setName(name);
		product.setPrice(price);
		product.setAvailability(availability);

		return product;
	}

	private ProductDetail productDetail(String id, String name, Double price, Boolean availability) {

		return new ProductDetail().id(id).name(name).price(price).availability(availability);
	}

}