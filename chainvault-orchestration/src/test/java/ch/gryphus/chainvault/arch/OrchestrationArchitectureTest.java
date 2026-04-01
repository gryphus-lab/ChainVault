/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.arch;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The type Orchestration architecture test.
 */
@AnalyzeClasses(
        packages = "ch.gryphus.chainvault",
        importOptions = ImportOption.DoNotIncludeTests.class)
class OrchestrationArchitectureTest {

    /**
     * The constant layered_architecture.
     */
    @ArchTest
    static final ArchRule layered_architecture =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Controller")
                    .definedBy("..controller..")
                    .layer("Service")
                    .definedBy("..service..")
                    .layer("Workflow")
                    .definedBy("..workflow..")
                    .layer("Repository")
                    .definedBy("..repository..")
                    .layer("Model")
                    .definedBy("..model..")

                    // Entry point
                    .whereLayer("Controller")
                    .mayNotBeAccessedByAnyLayer()

                    // Flow
                    .whereLayer("Workflow")
                    .mayOnlyBeAccessedByLayers("Controller")
                    .whereLayer("Repository")
                    .mayOnlyBeAccessedByLayers("Workflow")

                    // Entities can be used by repo + service + workflow
                    .whereLayer("Model")
                    .mayOnlyBeAccessedByLayers("Service", "Workflow", "Repository");
}
