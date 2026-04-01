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
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type General architecture test.
 */
@AnalyzeClasses(
        packages = "ch.gryphus.chainvault",
        importOptions = ImportOption.DoNotIncludeTests.class)
class GeneralArchitectureTest {

    /**
     * Debug imported classes.
     *
     * @param classes the classes
     */
    @ArchTest
    static void debug_imported_classes(JavaClasses classes) {
        System.out.println("Imported classes count: " + classes.size());
        classes.forEach(javaClass -> System.out.println("  - " + javaClass.getFullName()));
    }

    /**
     * The constant no_direct_persistence_access_from_controller.
     */
    @ArchTest
    static final ArchRule no_direct_persistence_access_from_controller =
            noClasses()
                    .that()
                    .resideInAPackage("..controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..persistence..");

    /**
     * The constant internal_modules_should_not_use_web_dependencies.
     */
    @ArchTest
    static final ArchRule internal_modules_should_not_use_web_dependencies =
            noClasses()
                    .that()
                    .resideInAPackage("..service..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..controller..");

    /**
     * The constant no_field_injection.
     */
    @ArchTest
    static final ArchRule no_field_injection =
            noClasses()
                    .should()
                    .beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");

    /**
     * The constant services_must_be_named_and_annotated_correctly.
     */
    @ArchTest
    static final ArchRule services_must_be_named_and_annotated_correctly =
            classes()
                    .that()
                    .resideInAPackage("..service..")
                    .should()
                    .haveSimpleNameEndingWith("Service")
                    .andShould()
                    .beAnnotatedWith(org.springframework.stereotype.Service.class);

    /**
     * The constant repo_naming.
     */
    @ArchTest
    static final ArchRule repo_naming =
            classes()
                    .that()
                    .resideInAPackage("..repository..")
                    .should()
                    .haveSimpleNameEndingWith("Repository");

    /**
     * The constant flowable_services_only_in_workflow_layer.
     */
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

    /**
     * The constant flowable_delegates_must_be_named_and_located_correctly.
     */
    @ArchTest
    static final ArchRule flowable_delegates_must_be_named_and_located_correctly =
            classes()
                    .that()
                    .implement(org.flowable.engine.delegate.JavaDelegate.class)
                    .should()
                    .haveSimpleNameEndingWith("Delegate")
                    .andShould()
                    .resideInAPackage("..workflow.delegate..");

    /**
     * The constant no_flowable_entities_in_controllers.
     */
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

    /**
     * The constant workflow_logic_must_be_transactional.
     */
    @ArchTest
    static final ArchRule workflow_logic_must_be_transactional =
            classes()
                    .that()
                    .resideInAPackage("..workflow..")
                    .and()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .beAnnotatedWith(Transactional.class);

    /**
     * The constant controllers_should_not_access_repositories_or_flowable_directly.
     */
    @ArchTest
    static final ArchRule controllers_should_not_access_repositories_or_flowable_directly =
            noClasses()
                    .that()
                    .resideInAnyPackage("..controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..repository..", "org.flowable.engine..", "org.flowable.task.api..");

    /**
     * The constant domain_should_not_depend_on_spring_or_flowable.
     */
    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring_or_flowable =
            noClasses()
                    .that()
                    .resideInAnyPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "org.flowable..");

    /**
     * The constant no_cycles_between_top_level_packages.
     */
    @ArchTest
    static final ArchRule no_cycles_between_top_level_packages =
            slices().matching("ch.gryphus.chainvault.(*)..").should().beFreeOfCycles();
}
