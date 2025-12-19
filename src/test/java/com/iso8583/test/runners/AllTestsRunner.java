package com.iso8583.test.runners;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber JUnit Platform Runner para TODOS los E2E Tests
 * VERSIÓN SIN SPRING BOOT
 *
 * Ejecuta todos los features:
 * - balance_inquiry.feature
 * - cash_advance.feature
 * - purchase.feature
 * - transfer.feature
 * - authorization.feature
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameters({
        @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
                value = "pretty," +
                        "html:target/cucumber-reports/all-tests.html," +
                        "json:target/cucumber-reports/all-tests.json," +
                        "junit:target/cucumber-reports/all-tests.xml," +
                        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"),
        @ConfigurationParameter(key = GLUE_PROPERTY_NAME,
                value = "com.iso8583.test.steps,com.iso8583.test.hooks,com.iso8583.test.config,com.iso8583.test.services,com.iso8583.test.client"),
        @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME,
                value = "not @Skip"),
        @ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME,
                value = "true"),
        @ConfigurationParameter(key = EXECUTION_DRY_RUN_PROPERTY_NAME,
                value = "false")
})
public class AllTestsRunner {
    // El runner está vacío - la configuración se hace mediante anotaciones
    /**
     * Este runner ejecuta TODOS los escenarios de TODOS los features
     * exceptuando los marcados con @Skip
     *
     * Para ejecutar:
     * mvn test -Dtest=AllTestsRunner
     *
     * Para ejecutar solo smoke tests:
     * mvn test -Dtest=AllTestsRunner -Dcucumber.filter.tags="@Smoke"
     *
     * Para ejecutar solo tests críticos:
     * mvn test -Dtest=AllTestsRunner -Dcucumber.filter.tags="@Critical"
     *
     * Para ejecutar solo tests de un tipo específico:
     * mvn test -Dtest=AllTestsRunner -Dcucumber.filter.tags="@BalanceInquiry"
     * mvn test -Dtest=AllTestsRunner -Dcucumber.filter.tags="@CashAdvance"
     * mvn test -Dtest=AllTestsRunner -Dcucumber.filter.tags="@Purchase"
     * mvn test -Dtest=AllTestsRunner -Dcucumber.filter.tags="@Transfer"
     * mvn test -Dtest=AllTestsRunner -Dcucumber.filter.tags="@Authorization"
     *
     * Para generar reporte Allure después:
     * mvn allure:serve
     *
     * Para ejecutar tests en paralelo (requiere configuración adicional):
     * mvn test -Dtest=AllTestsRunner -Dcucumber.execution.parallel.enabled=true
     */
}