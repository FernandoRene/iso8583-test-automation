package com.iso8583.test.hooks;

import com.iso8583.test.config.TestContext;
import com.iso8583.test.config.TestContextFactory;
import com.iso8583.test.utils.ScreenshotHelper;
import com.iso8583.test.utils.TestCoverageReporter;
import io.cucumber.java.*;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hooks de Cucumber mejorados con reportes avanzados
 */
public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private final TestContext testContext;
    private static boolean dashboardGenerated = false;

    public Hooks() {
        this.testContext = TestContextFactory.getInstance().getTestContext();
    }

    @BeforeAll
    public static void beforeAll() {
        logger.info("═".repeat(60));
        logger.info("🚀 INICIANDO SUITE DE TESTS ISO8583");
        logger.info("═".repeat(60));
        TestCoverageReporter.reset();
        dashboardGenerated = false;
    }

    @Before(order = 1)
    public void beforeScenario(Scenario scenario) {
        logger.info("─".repeat(60));
        logger.info("📋 ESCENARIO: {}", scenario.getName());
        logger.info("📂 Feature: {}", scenario.getUri());
        logger.info("🏷️  Tags: {}", scenario.getSourceTagNames());
        logger.info("─".repeat(60));

        testContext.reset();

        // Allure metadata
        Allure.epic("ISO8583 Test Automation");
        Allure.feature(getFeatureName(scenario));
        Allure.story(scenario.getName());
        scenario.getSourceTagNames().forEach(tag ->
                Allure.label("tag", tag.replace("@", ""))
        );

        logger.info("✅ TestContext reseteado para nuevo escenario");
    }

    @Before(value = "@RequiresConnection", order = 2)
    public void ensureConnectionForTaggedScenarios(Scenario scenario) {
        logger.info("🔌 Escenario requiere conexión - Asegurando conexión activa...");
        testContext.ensureConnection();
        logger.info("✅ Conexión asegurada para: {}", scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        String status = scenario.getStatus().toString();
        String emoji = scenario.isFailed() ? "❌" : "✅";

        logger.info("─".repeat(60));
        logger.info("{} RESULTADO: {} - {}", emoji, status, scenario.getName());

        try {
            // Capturar screenshot en failures
            if (scenario.isFailed()) {
                logger.warn("❌ Escenario FALLÓ - Capturando estado del sistema...");
                testContext.logCurrentState();

                Exception error = new RuntimeException("Scenario failed: " + scenario.getName());
                ScreenshotHelper.captureFailureState(testContext, error);

                boolean isConnected = testContext.getConnectionService().verifyAndReconnect();
                logger.info("🔌 Conexión después del fallo: {}", (isConnected ? "✅ CONECTADO" : "❌ DESCONECTADO"));
            }

            // Registrar en dashboard de cobertura
            if (testContext.getCurrentResponse() != null) {
                TestCoverageReporter.recordTransaction(
                        testContext.getCurrentResponse(),
                        getFeatureName(scenario),
                        testContext.getCurrentRequest() != null ?
                                testContext.getCurrentRequest().getTransactionType().toString() : "UNKNOWN"
                );
            }

            Allure.addAttachment("Scenario Status", status);

        } catch (Exception e) {
            logger.error("❌ Error en afterScenario: {}", e.getMessage(), e);
        }

        logger.info("─".repeat(60) + "\n");
    }

    @After(order = 0)
    public void captureFailureDetails(Scenario scenario) {
        if (scenario.isFailed()) {
            if (testContext.getLastResponse() != null) {
                String responseBody = testContext.getLastResponse().getBody().asString();
                int statusCode = testContext.getLastResponse().getStatusCode();

                logger.error("📊 Última respuesta antes del fallo:");
                logger.error("   Status Code: {}", statusCode);
                logger.error("   Response Body: {}", responseBody);

                scenario.attach(responseBody, "application/json", "Last Response");
            }

            boolean isConnected = testContext.isConnected();
            String connectionStatus = isConnected ? "CONECTADO" : "DESCONECTADO";

            logger.error("🔌 Estado de conexión al fallar: {}", connectionStatus);
            scenario.attach(connectionStatus, "text/plain", "Connection Status");
        }
    }

    @AfterAll
    public static void afterAll() {
        logger.info("═".repeat(60));
        logger.info("🏁 FINALIZANDO SUITE DE TESTS ISO8583");
        logger.info("═".repeat(60));

        if (!dashboardGenerated) {
            try {
                logger.info("📊 Generando Dashboard de Cobertura...");
                TestCoverageReporter.generateDashboard();
                dashboardGenerated = true;
                logger.info("✅ Dashboard generado exitosamente");
            } catch (Exception e) {
                logger.error("❌ Error generando dashboard: {}", e.getMessage(), e);
            }
        }

        logger.info("═".repeat(60));
        logger.info("✅ Suite de tests completada");
        logger.info("═".repeat(60));
    }

    private String getFeatureName(Scenario scenario) {
        String uri = scenario.getUri().toString();
        String[] parts = uri.split("/");
        String featureFile = parts[parts.length - 1];
        return featureFile.replace(".feature", "").replace("_", " ").toUpperCase();
    }
}