/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import org.flowable.common.engine.api.async.AsyncTaskExecutor;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
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

    /**
     * Custom flowable config engine configuration configurer.
     *
     * @param flowableTaskExecutor the flowable task executor
     * @return the engine configuration configurer
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> customFlowableConfig(
            AsyncTaskExecutor flowableTaskExecutor) {
        return engineConfiguration -> {
            engineConfiguration.setAsyncExecutorActivate(true);
            // Use a Spring-wrapped executor for Flowable's async tasks
            engineConfiguration.setAsyncTaskExecutor(flowableTaskExecutor);
        };
    }
}
