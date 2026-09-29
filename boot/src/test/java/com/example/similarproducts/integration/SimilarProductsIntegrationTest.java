package com.example.similarproducts.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimilarProductsIntegrationTest {

    private static final MockWebServer mockWebServer = createMockWebServer();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static MockWebServer createMockWebServer() {
        try {
            MockWebServer server = new MockWebServer();
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not start MockWebServer",
                    exception
            );
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "existing-api.base-url",
                () -> mockWebServer.url("/").toString()
        );
    }

    @AfterAll
    static void stopMockWebServer() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnAvailableProductsWhenOneSimilarProductFails()
            throws InterruptedException {

        // GET /product/1/similar
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        ["2", "3", "4"]
                        """));

        // GET product 2 -> OK
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "2",
                          "name": "Dress",
                          "price": 19.99,
                          "availability": true
                        }
                        """));

        // GET product 3 -> ERROR
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error": "Internal server error"
                        }
                        """));

        // GET product 4 -> OK
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "4",
                          "name": "Blazer",
                          "price": 29.99,
                          "availability": true
                        }
                        """));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/product/1/similar",
                String.class
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        assertThat(response.getBody())
                .contains("\"id\":\"2\"")
                .contains("\"id\":\"4\"")
                .doesNotContain("\"id\":\"3\"");

        
       // Primera llamada: obtener los IDs similares
        RecordedRequest similarRequest = mockWebServer.takeRequest();

        assertThat(similarRequest.getPath())
                .isEqualTo("/product/1/similarids");

        // Las llamadas a productos son concurrentes,
        // por lo que no asumimos su orden de llegada.
        List<String> productPaths = List.of(
                mockWebServer.takeRequest().getPath(),
                mockWebServer.takeRequest().getPath(),
                mockWebServer.takeRequest().getPath()
        );

        assertThat(productPaths)
                .containsExactlyInAnyOrder(
                        "/product/2",
                        "/product/3",
                        "/product/4"
                );

    }
    
}
