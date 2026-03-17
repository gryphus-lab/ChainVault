/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import io.opentelemetry.api.OpenTelemetry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * The type Open telemetry appender.
 */
@Component
class OpenTelemetryAppender implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    /**
     * Instantiates a new Open telemetry appender.
     *
     * @param openTelemetry the open telemetry
     */
    OpenTelemetryAppender(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender.install(
                openTelemetry);
    }
}
