package com.example.similarproducts.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.similarproducts.application.ports.driven.ProductRepositoryPort;
import com.example.similarproducts.application.ports.driving.GetSimilarProductsServicePort;
import com.example.similarproducts.application.services.GetSimilarProductsService;

@Configuration
public class ApplicationConfiguration {

    @Bean
    GetSimilarProductsServicePort getSimilarProductsService(
            ProductRepositoryPort productRepositoryPort) {

        return new GetSimilarProductsService(productRepositoryPort);
    }
}