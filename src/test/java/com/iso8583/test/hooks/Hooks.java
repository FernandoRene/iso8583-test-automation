package com.iso8583.test.hooks;

import com.iso8583.test.config.TestContext;
import com.iso8583.test.config.TestContextFactory;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;

/**
 * Hooks de Cucumber para inicialización y limpieza del contexto de prueba
 * Se ejecutan automáticamente antes/después de cada escenario
 * VERSIÓN SIN SPRING BOOT, SIN LOMBOK
 */
public class Hooks {

    private final TestContext testContext;

    public Hooks() {
        // ✅ Usar Singleton en lugar de inyección
        this.testContext = TestContextFactory.getInstance().getTestContext();
    }
    // Constructor manual (reemplaza @RequiredArgsConstructor de Lombok)
//    public Hooks(TestContext testContext) {
//        this.testContext = testContext;
//    }

    /**
     * Se ejecuta UNA VEZ antes de todos los escenarios
     */
//    @BeforeAll
//    public static void beforeAll() {
//        System.out.println("═══════════════════════════════════════════════════════════════");
//        System.out.println("🚀 INICIANDO SUITE DE PRUEBAS ISO8583");
//        System.out.println("═══════════════════════════════════════════════════════════════");
//    }

    /**
     * Se ejecuta ANTES de cada escenario
     */
    @Before(order = 1)
    public void beforeScenario(Scenario scenario) {
        System.out.println("───────────────────────────────────────────────────────────────");
        System.out.println("📝 ESCENARIO: " + scenario.getName());
        System.out.println("📂 Feature: " + scenario.getUri());
        System.out.println("🏷️  Tags: " + scenario.getSourceTagNames());
        System.out.println("───────────────────────────────────────────────────────────────");

        // Resetear contexto para nueva prueba
        testContext.reset();

        System.out.println("✅ TestContext reseteado para nuevo escenario");
    }

    /**
     * Se ejecuta ANTES de cada escenario - Asegurar conexión si tiene tag @RequiresConnection
     */
    @Before(value = "@RequiresConnection", order = 2)
    public void ensureConnectionForTaggedScenarios(Scenario scenario) {
        System.out.println("🔌 Escenario requiere conexión - Asegurando conexión activa...");
        testContext.ensureConnection();
        System.out.println("✅ Conexión asegurada para: " + scenario.getName());
    }

    /**
     * Se ejecuta DESPUÉS de cada escenario
     */
    @After
    public void afterScenario(Scenario scenario) {
        // Log del resultado
        String status = scenario.getStatus().toString();
        String emoji = scenario.isFailed() ? "❌" : "✅";

        System.out.println("───────────────────────────────────────────────────────────────");
        System.out.println(emoji + " RESULTADO: " + status + " - " + scenario.getName());

        // Si falló, log del estado actual para debugging
        if (scenario.isFailed()) {
            System.err.println("❌ ESCENARIO FALLÓ - Logging estado del contexto:");
            testContext.logCurrentState();

            // ✅ VERIFICAR CONEXIÓN SI FALLÓ
            System.out.println("🔌 Verificando estado de conexión después del fallo...");
            boolean isConnected = testContext.getConnectionService().verifyAndReconnect();
            System.out.println("📊 Conexión después del fallo: " + (isConnected ? "✅ CONECTADO" : "❌ DESCONECTADO"));
        }

        System.out.println("───────────────────────────────────────────────────────────────\n");
    }

//    @After(order = 1)
//    public void afterScenario(Scenario scenario) {
//        // Log del resultado
//        String status = scenario.getStatus().toString();
//        String emoji = scenario.isFailed() ? "❌" : "✅";
//
//        System.out.println("───────────────────────────────────────────────────────────────");
//        System.out.println(emoji + " RESULTADO: " + status + " - " + scenario.getName());
//
//        // Si falló, log del estado actual para debugging
//        if (scenario.isFailed()) {
//            System.err.println("❌ ESCENARIO FALLÓ - Logging estado del contexto:");
//            testContext.logCurrentState();
//        }
//
//        System.out.println("───────────────────────────────────────────────────────────────\n");
//    }

    /**
     * Se ejecuta DESPUÉS de cada escenario con tag @DisconnectAfter
     */
//    @After(value = "@DisconnectAfter", order = 2)
//    public void disconnectAfterTaggedScenarios(Scenario scenario) {
//        System.out.println("🔌 Escenario marcado para desconexión automática");
//
//        if (testContext.isConnected()) {
//            testContext.disconnect();
//            System.out.println("✅ Desconectado después de: " + scenario.getName());
//        }
//    }

    /**
     * Se ejecuta UNA VEZ después de todos los escenarios
     */
//    @AfterAll
//    public static void afterAll() {
//        System.out.println("═══════════════════════════════════════════════════════════════");
//        System.out.println("🏁 SUITE DE PRUEBAS COMPLETADA");
//        System.out.println("═══════════════════════════════════════════════════════════════");
//    }

    /**
     * Se ejecuta en caso de fallo - Captura screenshots o logs adicionales
     */
    @After(order = 0)
    public void captureFailureDetails(Scenario scenario) {
        if (scenario.isFailed()) {
            // Capturar estado de la última respuesta si existe
            if (testContext.getLastResponse() != null) {
                String responseBody = testContext.getLastResponse().getBody().asString();
                int statusCode = testContext.getLastResponse().getStatusCode();

                System.err.println("📊 Última respuesta antes del fallo:");
                System.err.println("   Status Code: " + statusCode);
                System.err.println("   Response Body: " + responseBody);

                // Adjuntar al reporte de Cucumber
                scenario.attach(responseBody, "application/json", "Last Response");
            }

            // Capturar estado de conexión
            boolean isConnected = testContext.isConnected();
            String connectionStatus = isConnected ? "CONECTADO" : "DESCONECTADO";

            System.err.println("🔌 Estado de conexión al fallar: " + connectionStatus);
            scenario.attach(connectionStatus, "text/plain", "Connection Status");
        }
    }
}