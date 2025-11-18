package com.iso8583.test.runners;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber JUnit Platform Runner para Cash Advance E2E Tests
 * VERSIÓN SIN SPRING BOOT
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/cash_advance.feature")
@ConfigurationParameters({
        @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
                value = "pretty," +
                        "html:target/cucumber-reports/cash-advance.html," +
                        "json:target/cucumber-reports/cash-advance.json," +
                        "junit:target/cucumber-reports/cash-advance.xml," +
                        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"),
        @ConfigurationParameter(key = GLUE_PROPERTY_NAME,
                value = "com.iso8583.test.steps,com.iso8583.test.hooks,com.iso8583.test.config,com.iso8583.test.services,com.iso8583.test.client"),
        @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME,
                value = "@CashAdvance and not @Skip"),
        @ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME,
                value = "true"),
        @ConfigurationParameter(key = EXECUTION_DRY_RUN_PROPERTY_NAME,
                value = "false")
})
public class CashAdvanceRunner {
    // El runner está vacío - la configuración se hace mediante anotaciones
    /**
     * Este runner ejecuta todos los escenarios del feature cash_advance.feature
     * que tengan el tag @CashAdvance y no tengan @Skip
     *
     * Para ejecutar:
     * mvn test -Dtest=CashAdvanceRunner
     *
     * Para ejecutar solo smoke tests:
     * mvn test -Dtest=CashAdvanceRunner -Dcucumber.filter.tags="@Smoke"
     *
     * Para generar reporte Allure después:
     * mvn allure:serve
     */
}