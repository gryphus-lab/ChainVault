/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Open api config.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Chain vault open api open api.
     *
     * @return the open api
     */
    @Bean
    public OpenAPI chainVaultOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("ChainVault API")
                                .version("0.0.1")
                                .description(
                                        "APIs for ChainVault orchestration and migration service")
                                .contact(
                                        new Contact()
                                                .name("gryphus-lab")
                                                .email("noreply@gryphus-lab")));
    }
}
