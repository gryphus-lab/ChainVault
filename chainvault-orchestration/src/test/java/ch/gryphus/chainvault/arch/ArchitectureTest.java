/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(
        packages = "ch.gryphus.chainvault",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static void debug_imported_classes(JavaClasses classes) {
        System.out.println("Imported classes count: " + classes.size());
        classes.forEach(javaClass -> System.out.println("  - " + javaClass.getFullName()));
    }

    @ArchTest
    static final ArchRule no_direct_persistence_access_from_controller =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage("..controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..persistence..");

    @ArchTest
    static final ArchRule internal_modules_should_not_use_web_dependencies =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage("..service..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule no_field_injection =
            ArchRuleDefinition.noClasses()
                    .should()
                    .beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");

    @ArchTest
    static final ArchRule naming_conventions =
            ArchRuleDefinition.classes()
                    .that()
                    .resideInAPackage("..service..")
                    .should()
                    .haveSimpleNameEndingWith("Service")
                    .andShould()
                    .beAnnotatedWith(org.springframework.stereotype.Service.class);

    @ArchTest
    static final ArchRule repo_naming =
            ArchRuleDefinition.classes()
                    .that()
                    .resideInAPackage("..repository..")
                    .should()
                    .haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule no_cycles =
            SlicesRuleDefinition.slices()
                    .matching("ch.gryphus.chainvault.(*)..") // Scans top-level modules/packages
                    .should()
                    .beFreeOfCycles();

    @ArchTest
    static final ArchRule flowable_services_only_in_workflow_layer =
            ArchRuleDefinition.classes()
                    .that()
                    .haveSimpleNameEndingWith("Service")
                    .and()
                    .resideOutsideOfPackage("..workflow..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideOutsideOfPackage("org.flowable.engine..");

    @ArchTest
    static final ArchRule delegates_naming_and_location =
            ArchRuleDefinition.classes()
                    .that()
                    .implement(org.flowable.engine.delegate.JavaDelegate.class)
                    .should()
                    .haveSimpleNameEndingWith("Delegate")
                    .andShould()
                    .resideInAPackage("..workflow.delegate..");

    @ArchTest
    static final ArchRule no_flowable_entities_in_controllers =
            ArchRuleDefinition.noClasses()
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
            ArchRuleDefinition.classes()
                    .that()
                    .resideInAPackage("..workflow..")
                    .and()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .beAnnotatedWith(
                            org.springframework.transaction.annotation.Transactional.class);
}
