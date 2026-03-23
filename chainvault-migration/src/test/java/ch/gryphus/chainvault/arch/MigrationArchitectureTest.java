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

@Slf4j
@AnalyzeClasses(
        packages = "ch.gryphus.chainvault",
        importOptions = ImportOption.DoNotIncludeTests.class)
class MigrationArchitectureTest {

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
