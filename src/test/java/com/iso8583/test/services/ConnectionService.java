package com.iso8583.test.services;

import com.iso8583.test.config.ConfigurationManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Servicio para gestionar la conexión con el simulador ISO8583
 * ✅ VERSIÓN CORREGIDA: Previene cambios de modo duplicados
 */
public class ConnectionService {

    private String baseUrl;
    private String simulatorMode;
    private boolean connected;
    private boolean noResponseMode;
    private static final Logger logger = LoggerFactory.getLogger(ConnectionService.class);

    /**
     * Constructor que recibe ConfigurationManager
     */
    public ConnectionService(ConfigurationManager configManager) {
        this.baseUrl = configManager.getBaseUrl();
        this.simulatorMode = "REAL";
        this.connected = false;
        this.noResponseMode = false;

        System.out.println("✅ ConnectionService inicializado - URL: " + baseUrl);
    }

    /**
     * Conecta al simulador y mantiene la conexión activa
     * ✅ MEJORADO: Lee información completa del modo
     */
    public void connect() {
        if (connected) {
            logger.info("🔌 Ya conectado al simulador");
            return;
        }

        logger.info("🔌 Conectando al simulador en: {}", baseUrl);

        try {
            Response response = RestAssured
                    .given()
                    .baseUri(baseUrl)
                    .post("/api/v1/connection/connect")
                    .then()
                    .extract()
                    .response();

            if (response.getStatusCode() == 200) {
                // ✅ Leer JSON completo de la respuesta
                Map<String, Object> responseBody = response.jsonPath().getMap("$");

                String mode = (String) responseBody.get("mode");
                Boolean tcpRequired = (Boolean) responseBody.get("tcpConnectionRequired");
                String simulatorType = (String) responseBody.get("simulatorType");

                connected = true;
                this.simulatorMode = mode; // Actualizar modo desde respuesta

                logger.info("✅ Conectado exitosamente al simulador");
                logger.info("   Modo: {}", mode);
                logger.info("   Simulador: {}", simulatorType);
                logger.info("   TCP requerido: {}", tcpRequired);

                // ✅ Esperar que la conexión se estabilice
                Thread.sleep(500);

            } else {
                logger.error("❌ Error conectando: {}", response.getStatusCode());
                logger.error("   Body: {}", response.getBody().asString());
                connected = false;
            }
        } catch (Exception e) {
            logger.error("❌ Error de conexión: {}", e.getMessage());
            connected = false;
        }
    }

    public void disconnect() {
        if (!connected) {
            logger.info("🔌 Ya desconectado del simulador");
            return;
        }

        logger.info("🔌 Desconectando del simulador...");

        try {
            Response response = RestAssured
                    .given()
                    .baseUri(baseUrl)
                    .post("/api/v1/connection/disconnect")
                    .then()
                    .extract()
                    .response();

            logger.info("✅ Desconectado: {}", response.getStatusCode());
        } catch (Exception e) {
            logger.warn("⚠️ Error desconectando: {}", e.getMessage());
        } finally {
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Asegura que hay una conexión activa
     */
    public void ensureConnection() {
        if (!connected) {
            logger.info("🔌 Asegurando conexión activa...");
            connect();
        } else {
            logger.debug("✅ Conexión ya está activa");
        }
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        System.out.println("🌐 URL base configurada: " + baseUrl);
    }

    public boolean isSimulatorAvailable() {
        try {
            Response response = RestAssured
                    .given()
                    .baseUri(baseUrl)
                    .get("/actuator/health")
                    .then()
                    .extract()
                    .response();

            return response.getStatusCode() == 200;
        } catch (Exception e) {
            System.err.println("⚠️ Simulador no disponible: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ FIX CRÍTICO: Previene cambios de modo innecesarios
     * Configura el modo del simulador y asegura la conexión
     */
    public void setSimulatorMode(String mode) {
        String targetMode = mode.toUpperCase();

        // ✅ FIX 1: Verificar si ya estamos en ese modo
        if (this.simulatorMode.equalsIgnoreCase(targetMode)) {
            logger.info("✅ Ya estamos en modo {}, omitiendo cambio", targetMode);
            return;
        }

        logger.info("🔄 Cambiando modo de {} a {}", this.simulatorMode, targetMode);

        // Asegurar conexión antes de cambiar modo
        ensureConnection();

        try {
            Response response = RestAssured
                    .given()
                    .baseUri(baseUrl)
                    .post("/api/v1/simulator/mode/{mode}", mode.toLowerCase())
                    .then()
                    .extract()
                    .response();

            if (response.getStatusCode() == 200) {
                this.simulatorMode = targetMode;
                logger.info("✅ Modo cambiado exitosamente a: {}", targetMode);

                // ✅ FIX 2: Esperar que el cambio de modo se complete
                Thread.sleep(1000); // Dar tiempo al simulador para reconectar

            } else {
                logger.error("❌ No se pudo cambiar el modo: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("❌ Error configurando modo: {}", e.getMessage());
        }
    }

    /**
     * ✅ FIX 3: Obtiene el estado REAL de la conexión desde el simulador
     */
    public Map<String, Object> getConnectionStatus() {
        try {
            Response response = RestAssured
                    .given()
                    .baseUri(baseUrl)
                    .get("/api/v1/connection/status")
                    .then()
                    .extract()
                    .response();

            if (response.getStatusCode() == 200) {
                return response.jsonPath().getMap("$");
            } else {
                logger.warn("⚠️ No se pudo obtener estado de conexión");
                return Map.of("connected", false, "channelConnected", false);
            }
        } catch (Exception e) {
            logger.error("❌ Error obteniendo estado: {}", e.getMessage());
            return Map.of("connected", false, "channelConnected", false);
        }
    }

    /**
     * Verifica el estado de la conexión y reconecta si es necesario
     */
    public boolean verifyAndReconnect() {
        if (!connected) {
            logger.warn("⚠️ Conexión perdida - reconectando...");
            connect();
            return connected;
        }

        try {
            Map<String, Object> status = getConnectionStatus();
            boolean isStillConnected = (boolean) status.getOrDefault("connected", false);

            if (!isStillConnected) {
                logger.warn("⚠️ Conexión reportada como inactiva - reconectando...");
                connected = false;
                connect();
            }

            return connected;
        } catch (Exception e) {
            logger.warn("⚠️ Error verificando estado de conexión - reconectando...");
            connected = false;
            connect();
            return connected;
        }
    }

    public void setNoResponseMode(boolean noResponse) {
        this.noResponseMode = noResponse;
        System.out.println("⏰ Modo sin respuesta: " + (noResponse ? "ACTIVADO" : "DESACTIVADO"));

        try {
            Response response = RestAssured
                    .given()
                    .baseUri(baseUrl)
                    .queryParam("noResponse", noResponse)
                    .post("/api/v1/config/no-response")
                    .then()
                    .extract()
                    .response();

            if (response.getStatusCode() == 200) {
                System.out.println("✅ Modo sin respuesta configurado");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error configurando modo sin respuesta: " + e.getMessage());
        }
    }

    public String getConnectionState() {
        return connected ? "CONNECTED" : "DISCONNECTED";
    }

    public ConnectionStatus getStatus() {
        Map<String, Object> realStatus = getConnectionStatus();

        boolean realConnected = (boolean) realStatus.getOrDefault("connected", false);
        boolean channelConnected = (boolean) realStatus.getOrDefault("channelConnected", false);

        // Sincronizar nuestro estado con el real
        if (!realConnected || !channelConnected) {
            connected = false;
        }

        return new ConnectionStatus(
                realConnected,
                channelConnected,
                baseUrl,
                simulatorMode,
                (String) realStatus.get("socketInfo")
        );


    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSimulatorMode() {
        return simulatorMode;
    }

    /**
     * ✅ MEJORADO: Clase de estado con más información
     */
    public static class ConnectionStatus {
        private final boolean connected;
        private final boolean channelConnected;
        private final String baseUrl;
        private final String mode;
        private final String socketInfo;

        public ConnectionStatus(boolean connected, boolean channelConnected,
                                String baseUrl, String mode, String socketInfo) {
            this.connected = connected;
            this.channelConnected = channelConnected;
            this.baseUrl = baseUrl;
            this.mode = mode;
            this.socketInfo = socketInfo;
        }

        public boolean isConnected() {
            return connected;
        }

        public boolean isChannelConnected() {
            return channelConnected;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getMode() {
            return mode;
        }

        public String getSocketInfo() {
            return socketInfo;
        }

        /**
         * Conexión completamente funcional = ambos true
         */
        public boolean isFullyConnected() {
            return connected && channelConnected;
        }
    }
}