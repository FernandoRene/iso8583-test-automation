package com.iso8583.test.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestor de configuración
 * Ahora con constructor PÚBLICO para que PicoContainer pueda instanciarlo
 */
public class ConfigurationManager {

    private final Properties properties;

    // Valores por defecto
    private static final String DEFAULT_BASE_URL = "http://localhost:8081";
    private static final String DEFAULT_TIMEOUT = "30000";
    private static final String DEFAULT_VALID_PAN = "4218281008687192";
    private static final String DEFAULT_VALID_TRACK2 = "4218281008687192D2709101123456789";
    private static final String DEFAULT_TERMINAL_ID = "ATM001LP";
    private static final String DEFAULT_CARD_ACCEPTOR_ID = "409911000001234";
    private static final String DEFAULT_ACCOUNT = "1310672399";
    private static final String DEFAULT_AMOUNT = "10000";
    private static final String DEFAULT_CURRENCY_CODE = "068";

    /**
     * Constructor PÚBLICO - Permite que PicoContainer lo instancie
     */
    public ConfigurationManager() {
        properties = new Properties();
        loadProperties();
        System.out.println("✅ ConfigurationManager inicializado");
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application-test.properties")) {

            if (input != null) {
                properties.load(input);
                System.out.println("✅ Configuración cargada desde application-test.properties");
            } else {
                System.out.println("⚠️ application-test.properties no encontrado, usando valores por defecto");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error cargando properties: " + e.getMessage());
        }
    }

    // Todos los getters quedan IGUAL

    public String getBaseUrl() {
        return properties.getProperty("simulator.base-url", DEFAULT_BASE_URL);
    }

    public int getTimeout() {
        return Integer.parseInt(properties.getProperty("simulator.timeout", DEFAULT_TIMEOUT));
    }

    public String getValidPan() {
        return properties.getProperty("test.data.valid-pan", DEFAULT_VALID_PAN);
    }

    public String getValidTrack2() {
        return properties.getProperty("test.data.valid-track2", DEFAULT_VALID_TRACK2);
    }

    public String getDefaultTerminalId() {
        return properties.getProperty("test.data.default-terminal", DEFAULT_TERMINAL_ID);
    }

    public String getDefaultCardAcceptorId() {
        return properties.getProperty("test.data.default-card-acceptor", DEFAULT_CARD_ACCEPTOR_ID);
    }

    public String getDefaultAccount() {
        return properties.getProperty("test.data.default-account", DEFAULT_ACCOUNT);
    }

    public String getDefaultAmount() {
        return properties.getProperty("test.data.default-amount", DEFAULT_AMOUNT);
    }

    public String getCurrencyCode() {
        return properties.getProperty("test.data.currency-code", DEFAULT_CURRENCY_CODE);
    }

    public String getTransactionEndpoint() {
        return getBaseUrl() + "/api/v1/transaction";
    }

    public String getConnectionEndpoint() {
        return getBaseUrl() + "/api/v1/connection";
    }

    public String getEndpointUrl(String endpoint) {
        return getBaseUrl() + endpoint;
    }
}