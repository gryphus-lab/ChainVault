/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(
        packages = "ch.gryphus.chainvault",
        importOptions = ImportOption.DoNotIncludeTests.class) // Scan your base package
class GlobalArchitectureTest {

    @ArchTest
    static void no_direct_persistence_access_from_controller(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..controller..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..persistence..");
    }

    @ArchTest
    static void internal_modules_should_not_use_web_dependencies(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..service..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..controller..");
    }

    @ArchTest
    static void no_field_injection(JavaClasses classes) {
        noClasses()
                .should()
                .beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");
    }

    @ArchTest
    static void naming_conventions(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..service..")
                .should()
                .haveSimpleNameEndingWith("Service")
                .andShould()
                .beAnnotatedWith(org.springframework.stereotype.Service.class);
    }

    @ArchTest
    static void repo_naming(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..repository..")
                .should()
                .haveSimpleNameEndingWith("Repository");
    }

    @ArchTest
    static void no_cycle(JavaClasses classes) {
        slices().matching("ch.gryphus.chainvault.(*)..") // Scans top-level modules/packages
                .should()
                .beFreeOfCycles();
    }

    @ArchTest
    static void flowable_services_only_in_workflow_layer(JavaClasses classes) {
        classes()
                .that()
                .haveSimpleNameEndingWith("Service")
                .and()
                .resideOutsideOfPackage("..workflow..")
                .should()
                .onlyDependOnClassesThat()
                .resideOutsideOfPackage("org.flowable.engine..");
    }

    @ArchTest
    static void delegates_naming_and_location(JavaClasses classes) {
        classes()
                .that()
                .implement(org.flowable.engine.delegate.JavaDelegate.class)
                .should()
                .haveSimpleNameEndingWith("Delegate")
                .andShould()
                .resideInAPackage("..workflow.delegate..");
    }

    @ArchTest
    static void no_flowable_entities_in_controllers(JavaClasses classes) {
        noClasses()
                .that()
                .resideInAPackage("..controller..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("org.flowable.engine.runtime..")
                .orShould()
                .dependOnClassesThat()
                .resideInAPackage("org.flowable.task.api..");
    }

    @ArchTest
    static void workflow_logic_must_be_transactional(JavaClasses classes) {
        classes()
                .that()
                .resideInAPackage("..workflow..")
                .and()
                .haveSimpleNameEndingWith("Service")
                .should()
                .beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class);
    }
}
