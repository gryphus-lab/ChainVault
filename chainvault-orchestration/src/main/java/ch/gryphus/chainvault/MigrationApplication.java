/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The type Migration application.
 */
@SpringBootApplication(proxyBeanMethods = false)
public class MigrationApplication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MigrationApplication.class, args);
    }
}
