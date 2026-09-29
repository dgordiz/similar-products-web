package com.example.similarproducts.driven.rest.repositories.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.existingapis.generated.model.ProductDetail;
import com.example.similarproducts.application.exceptions.ErrorCode;
import com.example.similarproducts.application.exceptions.SimilarProductsException;
import com.example.similarproducts.domain.ProductDTO;
import com.example.similarproducts.driven.rest.repositories.clients.ExistingApisClient;
import com.example.similarproducts.driven.rest.repositories.mappers.ExistingApisMapper;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

    private static final String PRODUCT_ID = "1";

    private static final String SIMILAR_PRODUCT_ID_1 = "2";
    private static final String SIMILAR_PRODUCT_ID_2 = "3";
    private static final String SIMILAR_PRODUCT_ID_3 = "4";

    @Mock
    private ExistingApisClient existingApisClient;

    @Mock
    private ExistingApisMapper mapper;

    @InjectMocks
    private ProductRepositoryAdapter adapter;

    private ProductDetail productDetail1;
    private ProductDetail productDetail2;
    private ProductDetail productDetail3;

    private ProductDTO productDTO1;
    private ProductDTO productDTO2;
    private ProductDTO productDTO3;

    @BeforeEach
    void setUp() {

        productDetail1 = new ProductDetail()
                .id(SIMILAR_PRODUCT_ID_1)
                .name("Product 2")
                .price(new BigDecimal("20.0"))
                .availability(true);

        productDetail2 = new ProductDetail()
                .id(SIMILAR_PRODUCT_ID_2)
                .name("Product 3")
                .price(new BigDecimal("30.0"))
                .availability(false);

        productDetail3 = new ProductDetail()
                .id(SIMILAR_PRODUCT_ID_3)
                .name("Product 4")
                .price(new BigDecimal("40.0"))
                .availability(true);

        productDTO1 = product(
                SIMILAR_PRODUCT_ID_1,
                "Product 2",
                new BigDecimal("20.0"),
                true
        );

        productDTO2 = product(
                SIMILAR_PRODUCT_ID_2,
                "Product 3",
                new BigDecimal("30.0"),
                false
        );

        productDTO3 = product(
                SIMILAR_PRODUCT_ID_3,
                "Product 4",
                new BigDecimal("40.0"),
                true
        );
    }

    @Test
    void shouldReturnSimilarProductsInSameOrder() {

        Set<String> similarProductIds = new LinkedHashSet<>(
                List.of(
                        SIMILAR_PRODUCT_ID_1,
                        SIMILAR_PRODUCT_ID_2,
                        SIMILAR_PRODUCT_ID_3
                )
        );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(Mono.just(productDetail1));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_2))
                .thenReturn(Mono.just(productDetail2));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_3))
                .thenReturn(Mono.just(productDetail3));

        when(mapper.toProductDTO(productDetail1))
                .thenReturn(productDTO1);

        when(mapper.toProductDTO(productDetail2))
                .thenReturn(productDTO2);

        when(mapper.toProductDTO(productDetail3))
                .thenReturn(productDTO3);

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result)
                .isInstanceOf(LinkedHashSet.class)
                .containsExactly(
                        productDTO1,
                        productDTO2,
                        productDTO3
                );

        verify(existingApisClient)
                .getProductSimilarIds(PRODUCT_ID);

        verify(existingApisClient)
                .getProductById(SIMILAR_PRODUCT_ID_1);

        verify(existingApisClient)
                .getProductById(SIMILAR_PRODUCT_ID_2);

        verify(existingApisClient)
                .getProductById(SIMILAR_PRODUCT_ID_3);

        verify(mapper).toProductDTO(productDetail1);
        verify(mapper).toProductDTO(productDetail2);
        verify(mapper).toProductDTO(productDetail3);
    }

    @Test
    void shouldReturnEmptySetWhenThereAreNoSimilarProducts() {

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(new LinkedHashSet<>()));

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result).isEmpty();

        verify(existingApisClient)
                .getProductSimilarIds(PRODUCT_ID);

        verifyNoMoreInteractions(existingApisClient);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void shouldThrowNotFoundWhenSimilarProductIdsRequestReturns404() {

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(
                        Mono.error(
                                webClientException(HttpStatus.NOT_FOUND)
                        )
                );

        assertThatThrownBy(
                () -> adapter.getSimilarProducts(PRODUCT_ID)
        )
                .isInstanceOf(SimilarProductsException.class)
                .hasMessage("Product not found");
    }

    @Test
    void shouldThrowGatewayTimeoutWhenSimilarProductIdsRequestReturns504() {

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(
                        Mono.error(
                                webClientException(HttpStatus.GATEWAY_TIMEOUT)
                        )
                );

        assertThatThrownBy(
                () -> adapter.getSimilarProducts(PRODUCT_ID)
        )
                .isInstanceOf(SimilarProductsException.class)
                .hasMessage(
                        "Timeout getting similar product ids for product "
                                + PRODUCT_ID
                );
    }

    @Test
    void shouldThrowGatewayTimeoutWhenSimilarProductIdsRequestTimesOut() {

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(
                        Mono.error(new TimeoutException())
                );

        assertThatThrownBy(
                () -> adapter.getSimilarProducts(PRODUCT_ID)
        )
                .isInstanceOf(SimilarProductsException.class)
                .hasMessage(
                        "Timeout getting similar product ids for product "
                                + PRODUCT_ID
                );
    }

    @Test
    void shouldThrowBadGatewayWhenSimilarProductIdsRequestFails() {

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(
                        Mono.error(
                                new RuntimeException("Connection error")
                        )
                );

        assertThatThrownBy(
                () -> adapter.getSimilarProducts(PRODUCT_ID)
        )
                .isInstanceOf(SimilarProductsException.class)
                .hasMessage(
                        "Error getting similar product ids for product "
                                + PRODUCT_ID
                );
    }

    @Test
    void shouldIgnoreProductWhenGettingProductReturns404() {

        Set<String> similarProductIds = new LinkedHashSet<>(
                List.of(
                        SIMILAR_PRODUCT_ID_1,
                        SIMILAR_PRODUCT_ID_2
                )
        );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(
                        Mono.error(
                                webClientException(HttpStatus.NOT_FOUND)
                        )
                );

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_2))
                .thenReturn(Mono.just(productDetail2));

        when(mapper.toProductDTO(productDetail2))
                .thenReturn(productDTO2);

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result)
                .containsExactly(productDTO2);

        verify(existingApisClient)
                .getProductById(SIMILAR_PRODUCT_ID_1);

        verify(existingApisClient)
                .getProductById(SIMILAR_PRODUCT_ID_2);

        verify(mapper)
                .toProductDTO(productDetail2);
    }

    @Test
    void shouldIgnoreProductWhenGettingProductReturns504() {

        Set<String> similarProductIds = new LinkedHashSet<>(
                List.of(
                        SIMILAR_PRODUCT_ID_1,
                        SIMILAR_PRODUCT_ID_2
                )
        );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(
                        Mono.error(
                                webClientException(HttpStatus.GATEWAY_TIMEOUT)
                        )
                );

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_2))
                .thenReturn(Mono.just(productDetail2));

        when(mapper.toProductDTO(productDetail2))
                .thenReturn(productDTO2);

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result)
                .containsExactly(productDTO2);

        verify(existingApisClient)
                .getProductById(SIMILAR_PRODUCT_ID_1);

        verify(existingApisClient)
                .getProductById(SIMILAR_PRODUCT_ID_2);

        verify(mapper)
                .toProductDTO(productDetail2);
    }

    @Test
    void shouldIgnoreProductWhenGettingProductTimesOut() {

        Set<String> similarProductIds = new LinkedHashSet<>(
                List.of(
                        SIMILAR_PRODUCT_ID_1,
                        SIMILAR_PRODUCT_ID_2
                )
        );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(
                        Mono.error(new TimeoutException())
                );

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_2))
                .thenReturn(Mono.just(productDetail2));

        when(mapper.toProductDTO(productDetail2))
                .thenReturn(productDTO2);

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result)
                .containsExactly(productDTO2);
    }

    @Test
    void shouldIgnoreProductWhenGettingProductFails() {

        Set<String> similarProductIds = new LinkedHashSet<>(
                List.of(
                        SIMILAR_PRODUCT_ID_1,
                        SIMILAR_PRODUCT_ID_2
                )
        );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(
                        Mono.error(
                                new RuntimeException("Connection error")
                        )
                );

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_2))
                .thenReturn(Mono.just(productDetail2));

        when(mapper.toProductDTO(productDetail2))
                .thenReturn(productDTO2);

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result)
                .containsExactly(productDTO2);
    }

    @Test
    void shouldReturnRemainingProductsInOriginalOrderWhenOneProductFails() {

        Set<String> similarProductIds = new LinkedHashSet<>(
                List.of(
                        SIMILAR_PRODUCT_ID_1,
                        SIMILAR_PRODUCT_ID_2,
                        SIMILAR_PRODUCT_ID_3
                )
        );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(Mono.just(productDetail1));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_2))
                .thenReturn(
                        Mono.error(
                                new RuntimeException("Connection error")
                        )
                );

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_3))
                .thenReturn(Mono.just(productDetail3));

        when(mapper.toProductDTO(productDetail1))
                .thenReturn(productDTO1);

        when(mapper.toProductDTO(productDetail3))
                .thenReturn(productDTO3);

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result)
                .isInstanceOf(LinkedHashSet.class)
                .containsExactly(
                        productDTO1,
                        productDTO3
                );
    }

    @Test
    void shouldReturnEmptySetWhenAllProductsFail() {

        Set<String> similarProductIds = new LinkedHashSet<>(
                List.of(
                        SIMILAR_PRODUCT_ID_1,
                        SIMILAR_PRODUCT_ID_2
                )
        );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(
                        Mono.error(new RuntimeException("Connection error"))
                );

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_2))
                .thenReturn(
                        Mono.error(new TimeoutException())
                );

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result).isEmpty();

        verifyNoMoreInteractions(mapper);
    }

    @Test
    void shouldPropagateSimilarProductsExceptionFromSimilarIds() {

        SimilarProductsException exception =
                new SimilarProductsException(
                        "Custom error",
                        ErrorCode.BAD_GATEWAY
                );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.error(exception));

        assertThatThrownBy(
                () -> adapter.getSimilarProducts(PRODUCT_ID)
        )
                .isSameAs(exception);
    }

    @Test
    void shouldIgnoreSimilarProductsExceptionFromIndividualProduct() {

        Set<String> similarProductIds = new LinkedHashSet<>(
                List.of(
                        SIMILAR_PRODUCT_ID_1,
                        SIMILAR_PRODUCT_ID_2
                )
        );

        SimilarProductsException exception =
                new SimilarProductsException(
                        "Custom error",
                        ErrorCode.BAD_GATEWAY
                );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(Mono.error(exception));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_2))
                .thenReturn(Mono.just(productDetail2));

        when(mapper.toProductDTO(productDetail2))
                .thenReturn(productDTO2);

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result)
                .containsExactly(productDTO2);
    }

    @Test
    void shouldUseMapperWhenGettingProduct() {

        Set<String> similarProductIds =
                new LinkedHashSet<>(
                        List.of(SIMILAR_PRODUCT_ID_1)
                );

        when(existingApisClient.getProductSimilarIds(PRODUCT_ID))
                .thenReturn(Mono.just(similarProductIds));

        when(existingApisClient.getProductById(SIMILAR_PRODUCT_ID_1))
                .thenReturn(Mono.just(productDetail1));

        when(mapper.toProductDTO(productDetail1))
                .thenReturn(productDTO1);

        Set<ProductDTO> result =
                adapter.getSimilarProducts(PRODUCT_ID);

        assertThat(result)
                .containsExactly(productDTO1);

        verify(mapper)
                .toProductDTO(productDetail1);
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

    private WebClientResponseException webClientException(
            HttpStatus status) {

        return WebClientResponseException.create(
                status.value(),
                status.getReasonPhrase(),
                HttpHeaders.EMPTY,
                null,
                null
        );
    }
}
