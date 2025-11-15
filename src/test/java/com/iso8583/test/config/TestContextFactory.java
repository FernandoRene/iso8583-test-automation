package com.iso8583.test.config;

import com.iso8583.test.client.ISO8583ApiClient;
import com.iso8583.test.services.ConnectionService;
import com.iso8583.test.services.TransactionService;

/**
 * Factory para crear TestContext - VERSIÓN SINGLETON
 * Se crea una sola instancia por JVM y se reutiliza
 */
public class TestContextFactory {

    private static TestContextFactory instance;
    private final TestContext testContext;

    private TestContextFactory() {
        System.out.println("🏭 TestContextFactory - Inicializando servicios SINGLETON...");

        // Crear ConfigurationManager
        ConfigurationManager configManager = new ConfigurationManager();

        // Crear API Client
        ISO8583ApiClient apiClient = new ISO8583ApiClient(configManager);

        // Crear Connection Service
        ConnectionService connectionService = new ConnectionService(configManager);

        // Crear Transaction Service SIN TestContext inicialmente
        TransactionService transactionService = new TransactionService(apiClient);

        // Crear TestContext
        this.testContext = new TestContext(transactionService, connectionService);

        // Vincular TestContext a TransactionService DESPUÉS
        transactionService.setTestContext(this.testContext);

        System.out.println("✅ TestContextFactory - Todos los servicios listos (SINGLETON)");
    }

    public static synchronized TestContextFactory getInstance() {
        if (instance == null) {
            instance = new TestContextFactory();
        }
        return instance;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    /**
     * Reset del contexto para nuevo escenario
     */
    public void resetContext() {
        testContext.reset();
        System.out.println("🔄 TestContext reseteado para nuevo escenario");
    }
}