/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.arch;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import lombok.extern.slf4j.Slf4j;

/**
 * ArchUnit tests for {@code MigrationArchitectureTest} that validate architectural constraints
 * in the chainvault-migration module.
 *
 * <p>This test class enforces a layered architecture with the following rules:
 * <ul>
 *   <li>Service layer is top-level and may not be accessed by any other layer</li>
 *   <li>Config, Util, and Exception layers may only be accessed by the Service layer</li>
 * </ul>
 *
 * <p>Scope: All production code in {@code ch.gryphus.chainvault} (test classes excluded).
 */
@Slf4j
@AnalyzeClasses(
        packages = "ch.gryphus.chainvault",
        importOptions = ImportOption.DoNotIncludeTests.class)
class MigrationArchitectureTest {

    /**
     * Validates the layered architecture constraint for the chainvault-migration module.
     *
     * <p>This rule defines four layers (Service, Config, Util, Exception) and enforces that:
     * <ul>
     *   <li>Service layer is the outermost layer and may not be accessed by any other layer</li>
     *   <li>Config, Util, and Exception layers are internal and may only be accessed by Service</li>
     * </ul>
     *
     * <p>This ensures a clean top-down dependency flow where services orchestrate lower-level
     * utilities, configuration, and exception handling, preventing circular dependencies and
     * unintended coupling between internal layers.
     */
    @ArchTest
    static final ArchRule layered_architecture =
            layeredArchitecture()
                    .consideringAllDependencies()
                    // define te layers
                    .layer("Service")
                    .definedBy("..service..")
                    .layer("Config")
                    .definedBy("..config..")
                    .layer("Util")
                    .definedBy("..util..")
                    .layer("Exception")
                    .definedBy("..exception..")

                    // enforce layer restrictions
                    .whereLayer("Service")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Config")
                    .mayOnlyBeAccessedByLayers("Service")
                    .whereLayer("Util")
                    .mayOnlyBeAccessedByLayers("Service")
                    .whereLayer("Exception")
                    .mayOnlyBeAccessedByLayers("Service");
}