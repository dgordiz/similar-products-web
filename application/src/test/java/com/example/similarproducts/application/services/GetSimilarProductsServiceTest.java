package com.example.similarproducts.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.example.similarproducts.application.exceptions.SimilarProductsException;
import com.example.similarproducts.application.ports.driven.ProductRepositoryPort;
import com.example.similarproducts.domain.ProductDTO;

import reactor.core.publisher.Mono;

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

        ProductDTO product1 = product("1", "Product 1", new BigDecimal(10.0), true);
        ProductDTO product2 = product("2", "Product 2", new BigDecimal(20.0), true);
        ProductDTO product3 = product("3", "Product 3", new BigDecimal(30.0), false);

        when(productRepositoryPort.getSimilarProductIds("1"))
                .thenReturn(Mono.just(List.of("1", "2", "3")));

        when(productRepositoryPort.getProduct("1"))
                .thenReturn(Mono.just(product1));

        when(productRepositoryPort.getProduct("2"))
                .thenReturn(Mono.just(product2));

        when(productRepositoryPort.getProduct("3"))
                .thenReturn(Mono.just(product3));

        List<ProductDTO> result =
                service.getSimilarProducts("1").block();

        assertThat(result)
                .containsExactly(product1, product2, product3);
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoSimilarProducts() {

        when(productRepositoryPort.getSimilarProductIds("1"))
                .thenReturn(Mono.just(List.of()));

        List<ProductDTO> result =
                service.getSimilarProducts("1").block();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldPropagateNotFoundError() {

        when(productRepositoryPort.getSimilarProductIds("1"))
                .thenReturn(Mono.just(List.of("2")));

        SimilarProductsException exception =
                new SimilarProductsException(
                        "Product not found: 2",
                        HttpStatus.NOT_FOUND
                );

        when(productRepositoryPort.getProduct("2"))
                .thenReturn(Mono.error(exception));

        SimilarProductsException thrown =
                org.junit.jupiter.api.Assertions.assertThrows(
                        SimilarProductsException.class,
                        () -> service.getSimilarProducts("1").block()
                );

        assertThat(thrown.getErrorCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(thrown.getMessage())
                .isEqualTo("Product not found: 2");
    }

    @Test
    void shouldPropagateRepositoryError() {

        when(productRepositoryPort.getSimilarProductIds("1"))
                .thenReturn(Mono.just(List.of("2")));

        SimilarProductsException exception =
                new SimilarProductsException(
                        "Error getting product 2",
                        HttpStatus.BAD_GATEWAY
                );

        when(productRepositoryPort.getProduct("2"))
                .thenReturn(Mono.error(exception));

        SimilarProductsException thrown =
                org.junit.jupiter.api.Assertions.assertThrows(
                        SimilarProductsException.class,
                        () -> service.getSimilarProducts("1").block()
                );

        assertThat(thrown.getErrorCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    private ProductDTO product(
            String id,
            String name,
            BigDecimal price,
            boolean availability) {

        ProductDTO product = new ProductDTO();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setAvailability(availability);

        return product;
    }
}
