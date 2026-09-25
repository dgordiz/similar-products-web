package com.example.similarproducts.driving.controllers.adapters;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.similarproducts.application.exceptions.SimilarProductsException;
import com.example.similarproducts.application.ports.driving.GetSimilarProductsServicePort;
import com.example.similarproducts.domain.ProductDTO;
import com.example.similarproducts.driving.controllers.handlers.GlobalExceptionHandler;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SimilarProductsControllerTest {

    @Mock
    private GetSimilarProductsServicePort useCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        SimilarProductsController controller =
                new SimilarProductsController(useCase);

        webTestClient = WebTestClient
                .bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnSimilarProducts() {

        ProductDTO product = new ProductDTO();
        product.setId("2");
        product.setName("Product 2");
        product.setPrice(new BigDecimal(20.5));
        product.setAvailability(true);

        when(useCase.getSimilarProducts("1"))
                .thenReturn(Mono.just(List.of(product)));

        webTestClient
                .get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        [
                          {
                            "id": "2",
                            "name": "Product 2",
                            "price": 20.5,
                            "availability": true
                          }
                        ]
                        """);
    }

    @Test
    void shouldPropagateNotFoundError() {

        SimilarProductsException exception =
                new SimilarProductsException(
                        "Product not found: 1",
                        HttpStatus.NOT_FOUND
                );

        when(useCase.getSimilarProducts("1"))
                .thenReturn(Mono.error(exception));

        webTestClient
                .get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("Product not found: 1");
    }

    @Test
    void shouldPropagateBadGatewayError() {

        SimilarProductsException exception =
                new SimilarProductsException(
                        "Error getting product 1",
                        HttpStatus.BAD_GATEWAY
                );

        when(useCase.getSimilarProducts("1"))
                .thenReturn(Mono.error(exception));

        webTestClient
                .get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_GATEWAY)
                .expectBody(String.class)
                .isEqualTo("Error getting product 1");
    }
}
