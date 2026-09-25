package com.example.similarproducts.driven.rest.repositories.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.similarproducts.application.exceptions.SimilarProductsException;
import com.example.similarproducts.domain.ProductDTO;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

class ProductRepositoryAdapterTest {

    private MockWebServer mockWebServer;

    private ProductRepositoryAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .build();

        adapter = new ProductRepositoryAdapter(webClient);

        String baseUrl = mockWebServer.url("").toString();

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        ReflectionTestUtils.setField(adapter, "baseUrl", baseUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnSimilarProductIds() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                                ["1", "2", "3"]
                                """)
        );

        List<String> result = adapter
                .getSimilarProductIds("1")
                .block();

        assertThat(result)
                .containsExactly("1", "2", "3");

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getMethod())
                .isEqualTo("GET");

        assertThat(request.getPath())
                .isEqualTo("/product/1/similarids");
    }

    @Test
    void shouldReturnNotFoundWhenSimilarProductIdsProductDoesNotExist()
            throws Exception {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
        );

        assertThatThrownBy(() ->
                adapter.getSimilarProductIds("999").block()
        )
                .isInstanceOf(SimilarProductsException.class)
                .satisfies(error -> {
                    SimilarProductsException exception =
                            (SimilarProductsException) error;

                    assertThat(exception.getErrorCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);

                    assertThat(exception.getMessage())
                            .isEqualTo("Product not found: 999");
                });

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getPath())
                .isEqualTo("/product/999/similarids");
    }

    @Test
    void shouldReturnBadGatewayWhenGettingSimilarProductIdsFails()
            throws Exception {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(500)
        );

        assertThatThrownBy(() ->
                adapter.getSimilarProductIds("1").block()
        )
                .isInstanceOf(SimilarProductsException.class)
                .satisfies(error -> {
                    SimilarProductsException exception =
                            (SimilarProductsException) error;

                    assertThat(exception.getErrorCode())
                            .isEqualTo(HttpStatus.BAD_GATEWAY);

                    assertThat(exception.getMessage())
                            .isEqualTo(
                                    "Error getting similar product ids "
                                            + "for product 1"
                            );
                });

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getPath())
                .isEqualTo("/product/1/similarids");
    }

    @Test
    void shouldReturnProduct() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                                {
                                  "id": "1",
                                  "name": "Product 1",
                                  "price": 10.0,
                                  "availability": true
                                }
                                """)
        );

        ProductDTO result = adapter
                .getProduct("1")
                .block();

        assertThat(result)
                .isNotNull();

        assertThat(result.getId())
                .isEqualTo("1");

        assertThat(result.getName())
                .isEqualTo("Product 1");

        assertThat(result.getPrice())
                .isEqualByComparingTo(new BigDecimal("10.0"));

        assertThat(result.getAvailability())
                .isTrue();

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getMethod())
                .isEqualTo("GET");

        assertThat(request.getPath())
                .isEqualTo("/product/1");
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist()
            throws Exception {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
        );

        assertThatThrownBy(() ->
                adapter.getProduct("999").block()
        )
                .isInstanceOf(SimilarProductsException.class)
                .satisfies(error -> {
                    SimilarProductsException exception =
                            (SimilarProductsException) error;

                    assertThat(exception.getErrorCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);

                    assertThat(exception.getMessage())
                            .isEqualTo("Product not found: 999");
                });

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getPath())
                .isEqualTo("/product/999");
    }

    @Test
    void shouldReturnBadGatewayWhenGettingProductFails()
            throws Exception {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(500)
        );

        assertThatThrownBy(() ->
                adapter.getProduct("1").block()
        )
                .isInstanceOf(SimilarProductsException.class)
                .satisfies(error -> {
                    SimilarProductsException exception =
                            (SimilarProductsException) error;

                    assertThat(exception.getErrorCode())
                            .isEqualTo(HttpStatus.BAD_GATEWAY);

                    assertThat(exception.getMessage())
                            .isEqualTo("Error getting product 1");
                });

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getPath())
                .isEqualTo("/product/1");
    }
}