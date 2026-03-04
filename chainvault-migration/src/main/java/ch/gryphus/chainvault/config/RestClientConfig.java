/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * The type Rest client config.
 */
@Configuration
public class RestClientConfig {

    /**
     * Rest client rest client.
     *
     * @param baseUrl the base url
     * @param token the token
     * @return  the rest client
     */
    @Bean
    public RestClient restClient(
            @Value("${source.api.base-url:https://legacy-api.example.com}") String baseUrl,
            @Value("${source.api.token:}") String token) {

        var builder = RestClient.builder().baseUrl(baseUrl);

        if (!token.isBlank()) {
            builder = builder.defaultHeader("Authorization", "Bearer " + token);
        }

        return builder.build();
    }
}
