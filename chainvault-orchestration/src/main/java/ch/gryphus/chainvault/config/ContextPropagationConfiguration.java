/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

/**
 * The type Context propagation configuration.
 */
@Configuration(proxyBeanMethods = false)
public class ContextPropagationConfiguration {

    /**
     * Context propagating task decorator context propagating task decorator.
     *
     * @return the context propagating task decorator
     */
    @Bean
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }
}
