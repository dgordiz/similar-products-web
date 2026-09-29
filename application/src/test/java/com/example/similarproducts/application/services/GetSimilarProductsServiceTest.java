package com.example.similarproducts.application.services;

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

import com.example.similarproducts.application.ports.driven.ProductRepositoryPort;
import com.example.similarproducts.domain.ProductDTO;

@ExtendWith(MockitoExtension.class)
class GetSimilarProductsServiceTest {

	@Mock
	private ProductRepositoryPort productRepositoryPort;

	private GetSimilarProductsService service;

	@BeforeEach
	void setUp() {
		service = new GetSimilarProductsService(productRepositoryPort);
	}

	@Test
	void shouldReturnSimilarProductsInSameOrder() {

		ProductDTO product1 = product("1", "Product 1", new BigDecimal("10.0"), true);

		ProductDTO product2 = product("2", "Product 2", new BigDecimal("20.0"), true);

		ProductDTO product3 = product("3", "Product 3", new BigDecimal("30.0"), false);

		Set<ProductDTO> expected = new LinkedHashSet<>();
		expected.add(product1);
		expected.add(product2);
		expected.add(product3);

		when(productRepositoryPort.getSimilarProducts("1")).thenReturn(expected);

		Set<ProductDTO> result = service.getSimilarProducts("1");

		assertThat(result).containsExactly(product1, product2, product3);

		verify(productRepositoryPort).getSimilarProducts("1");
	}

	@Test
	void shouldReturnEmptySetWhenThereAreNoSimilarProducts() {

		when(productRepositoryPort.getSimilarProducts("1")).thenReturn(new LinkedHashSet<>());

		Set<ProductDTO> result = service.getSimilarProducts("1");

		assertThat(result).isEmpty();

		verify(productRepositoryPort).getSimilarProducts("1");
	}

	@Test
	void shouldPropagateRepositoryException() {

		RuntimeException exception = new RuntimeException("Repository error");

		when(productRepositoryPort.getSimilarProducts("1")).thenThrow(exception);

		assertThat(org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
				() -> service.getSimilarProducts("1"))).isSameAs(exception);

		verify(productRepositoryPort).getSimilarProducts("1");
	}

	private ProductDTO product(String id, String name, BigDecimal price, boolean availability) {

		ProductDTO product = new ProductDTO();
		product.setId(id);
		product.setName(name);
		product.setPrice(price);
		product.setAvailability(availability);

		return product;
	}
}