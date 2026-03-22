/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "ch.gryphus.chainvault", importOptions = ImportOption.DoNotIncludeTests.class) // Scan your base package
class GlobalArchitectureTest {

    @ArchTest
    static final ArchRule no_direct_persistence_access_from_controller =
            noClasses()
                    .that()
                    .resideInAPackage("..controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..persistence..");

    @ArchTest
    static final ArchRule internal_modules_should_not_use_web_dependencies =
            noClasses()
                    .that()
                    .resideInAPackage("..service..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule no_field_injection =
            noClasses()
                    .should()
                    .beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");

    @ArchTest
    static final ArchRule naming_conventions =
            classes()
                    .that()
                    .resideInAPackage("..service..")
                    .should()
                    .haveSimpleNameEndingWith("Service")
                    .andShould()
                    .beAnnotatedWith(org.springframework.stereotype.Service.class);

    @ArchTest
    static final ArchRule repo_naming =
            classes()
                    .that()
                    .resideInAPackage("..repository..")
                    .should()
                    .haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule no_cycles =
            slices().matching("ch.gryphus.chainvault.(*)..") // Scans top-level modules/packages
                    .should()
                    .beFreeOfCycles();

    @ArchTest
    static final ArchRule flowable_services_only_in_workflow_layer =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Service")
                    .and()
                    .resideOutsideOfPackage("..workflow..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideOutsideOfPackage("org.flowable.engine..");

    @ArchTest
    static final ArchRule delegates_naming_and_location =
            classes()
                    .that()
                    .implement(org.flowable.engine.delegate.JavaDelegate.class)
                    .should()
                    .haveSimpleNameEndingWith("Delegate")
                    .andShould()
                    .resideInAPackage("..workflow.delegate..");

    @ArchTest
    static final ArchRule no_flowable_entities_in_controllers =
            noClasses()
                    .that()
                    .resideInAPackage("..controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("org.flowable.engine.runtime..")
                    .orShould()
                    .dependOnClassesThat()
                    .resideInAPackage("org.flowable.task.api..");

    @ArchTest
    static final ArchRule workflow_logic_must_be_transactional =
            classes()
                    .that()
                    .resideInAPackage("..workflow..")
                    .and()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .beAnnotatedWith(
                            org.springframework.transaction.annotation.Transactional.class);
}
