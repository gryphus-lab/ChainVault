/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class MigrationApplicationTest {

    @Test
    void testMainWithMock() {
        try (MockedStatic<SpringApplication> springApplicationMock =
                mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            springApplicationMock
                    .when(() -> SpringApplication.run(MigrationApplication.class, new String[] {}))
                    .thenReturn(mockContext);

            MigrationApplication.main(new String[] {});

            springApplicationMock.verify(
                    () -> SpringApplication.run(MigrationApplication.class, new String[] {}));
        }
    }
}
