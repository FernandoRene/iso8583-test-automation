package com.iso8583.test.config;

import com.iso8583.test.models.TransactionRequest;
import com.iso8583.test.models.TransactionResponse;
import com.iso8583.test.models.TransactionType;
import com.iso8583.test.services.ConnectionService;
import com.iso8583.test.services.TransactionService;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Contexto de prueba compartido entre steps de Cucumber
 * Maneja el estado de la prueba actual y proporciona acceso a servicios
 * VERSIÓN SIN SPRING BOOT, SIN LOMBOK
 *
 * ✅ VERSIÓN CORREGIDA con sincronización de responses
 */
public class TestContext {

    private static final Logger logger = LoggerFactory.getLogger(TestContext.class);

    private final TransactionService transactionService;
    private final ConnectionService connectionService;

    // Estado de transacciones
    private TransactionRequest.Builder requestBuilder;
    private TransactionRequest currentRequest;
    private TransactionResponse currentResponse;
    private final List<TransactionResponse> multipleResponses = new ArrayList<>();
    private TransactionType currentTransactionType;

    // Estado de tiempo/performance
    private Long transactionStartTime;
    private Long transactionEndTime;

    // Estado de conexión
    private boolean connectionInitialized = false;

    // Estado de respuesta REST (RestAssured Response)
    private Response lastResponse;


    // Constructor
    public TestContext(TransactionService transactionService,
                       ConnectionService connectionService) {
        this.transactionService = transactionService;
        this.connectionService = connectionService;

        logger.info("✅ TestContext creado para nuevo escenario");
    }



    // ============================================================================
    // GETTERS Y SETTERS
    // ============================================================================

    public TransactionRequest.Builder getRequestBuilder() {
        if (this.requestBuilder == null) {
            // Inicializar automáticamente si es null
            this.requestBuilder = TransactionRequest.builder();
            logger.debug("🆕 RequestBuilder auto-inicializado");
        }
        return this.requestBuilder;
    }

    public void setRequestBuilder(TransactionRequest.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public TransactionRequest getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(TransactionRequest currentRequest) {
        this.currentRequest = currentRequest;
        logger.debug("📝 CurrentRequest guardado: {}", currentRequest.getTransactionType());
    }

    public TransactionResponse getCurrentResponse() {
        if (currentResponse == null) {
            throw new IllegalStateException("No hay respuesta actual disponible");
        }
        return currentResponse;
    }

    /**
     * ✅ MÉTODO CORREGIDO - Ahora sincroniza currentResponse y lastResponse
     *
     * @param response TransactionResponse del simulador
     */
    public void setCurrentResponse(TransactionResponse response) {
        this.currentResponse = response;
        stopTransactionTimer();

        logger.info("📥 Respuesta guardada - Success: {}, Code: {}, STAN: {}",
                response.getSuccessful(),
                response.getResponseCode(),
                response.getStan());

        logger.debug("✅ CurrentResponse sincronizado en TestContext");
    }


    /**
     * ✅ NUEVO MÉTODO - Sincroniza AMBAS responses al mismo tiempo
     * Este método debe ser llamado por TransactionService
     *
     * @param transactionResponse TransactionResponse parseada
     * @param restAssuredResponse Response original de RestAssured
     */
    public void setResponses(TransactionResponse transactionResponse, Response restAssuredResponse) {
        this.currentResponse = transactionResponse;
        this.lastResponse = restAssuredResponse;
        stopTransactionTimer();

        logger.info("📥 Respuestas sincronizadas - Success: {}, HTTP Status: {}, STAN: {}",
                transactionResponse.getSuccessful(),
                restAssuredResponse.getStatusCode(),
                transactionResponse.getStan());

        logger.debug("✅ Ambas responses guardadas en TestContext");
    }

    public List<TransactionResponse> getMultipleResponses() {
        return new ArrayList<>(multipleResponses);
    }

    public TransactionType getCurrentTransactionType() {
        return currentTransactionType;
    }

    public void setCurrentTransactionType(TransactionType currentTransactionType) {
        this.currentTransactionType = currentTransactionType;
        logger.debug("📂 TransactionType configurado: {}", currentTransactionType);
    }

    public Long getTransactionStartTime() {
        return transactionStartTime;
    }

    public void setTransactionStartTime(Long transactionStartTime) {
        this.transactionStartTime = transactionStartTime;
    }

    public Long getTransactionEndTime() {
        return transactionEndTime;
    }

    public void setTransactionEndTime(Long transactionEndTime) {
        this.transactionEndTime = transactionEndTime;
    }

    public boolean isConnectionInitialized() {
        return connectionInitialized;
    }

    public void setConnectionInitialized(boolean connectionInitialized) {
        this.connectionInitialized = connectionInitialized;
        logger.debug("🔌 ConnectionInitialized: {}", connectionInitialized);
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
        logger.debug("📥 LastResponse (RestAssured) guardado - Status: {}",
                lastResponse != null ? lastResponse.getStatusCode() : "null");
    }

    // ============================================================================
    // MÉTODOS DE INICIALIZACIÓN
    // ============================================================================

    public void reset() {
        logger.info("🔄 Reseteando TestContext");

        this.requestBuilder = null;
        this.currentRequest = null;
        this.currentResponse = null;
        this.multipleResponses.clear();
        this.currentTransactionType = null;
        this.transactionStartTime = null;
        this.transactionEndTime = null;
        this.lastResponse = null;

        logger.info("✅ TestContext reseteado");
    }


    public void ensureConnection() {
        logger.info("🔌 Asegurando conexión activa con el simulador...");

        if (!connectionInitialized) {
            logger.info("🔌 Inicializando conexión por primera vez...");
            connectionService.ensureConnection();
            connectionInitialized = true;
            logger.info("✅ Conexión inicializada y activa");
        } else {
            // Verificar y reconectar si es necesario
            boolean isStillConnected = connectionService.verifyAndReconnect();
            if (!isStillConnected) {
                logger.warn("⚠️ Conexión se perdió - reintentando...");
                connectionService.connect();
            }
            logger.info("✅ Conexión verificada y activa");
        }
    }

//    public void ensureConnection() {
//        if (!connectionInitialized) {
//            logger.info("🔌 Inicializando conexión automáticamente...");
//            connectionService.ensureConnection();
//            connectionInitialized = true;
//            logger.info("✅ Conexión inicializada");
//        } else {
//            logger.debug("✅ Conexión ya inicializada");
//        }
//    }

    // ============================================================================
    // BUILDER PARA TRANSACCIONES
    // ============================================================================

    public TransactionRequest.Builder startNewRequest() {
        logger.info("🆕 Iniciando nuevo request builder");

        this.requestBuilder = TransactionRequest.builder();
        this.currentRequest = null;
        this.currentResponse = null;

        return this.requestBuilder;
    }

    public TransactionRequest.Builder newTransactionBuilder(TransactionType transactionType) {
        logger.info("🗂️ Creando nuevo builder para: {}", transactionType);

        this.currentTransactionType = transactionType;
        this.requestBuilder = TransactionRequest.builder()
                .transactionType(transactionType)
                .applyDefaults();

        return this.requestBuilder;
    }

    public TransactionRequest.Builder getOrCreateBuilder(TransactionType transactionType) {
        if (this.requestBuilder == null || this.currentTransactionType != transactionType) {
            return newTransactionBuilder(transactionType);
        }
        return this.requestBuilder;
    }

    public TransactionRequest buildCurrentRequest() {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("No hay builder de transacción activo");
        }

        TransactionRequest request = this.requestBuilder.build();
        logger.info("📦 Request construido - Tipo: {}", request.getTransactionType());

        return request;
    }

    public TransactionRequest buildAndSetRequest() {
        TransactionRequest request = buildCurrentRequest();
        this.currentRequest = request;

        logger.info("📦 Request construido y guardado - Tipo: {}", request.getTransactionType());

        return request;
    }

    // ============================================================================
    // GESTIÓN DE TIEMPO/PERFORMANCE
    // ============================================================================

    public void startTransactionTimer() {
        this.transactionStartTime = System.currentTimeMillis();
        logger.debug("⏱️ Timer de transacción iniciado");
    }

    public void stopTransactionTimer() {
        this.transactionEndTime = System.currentTimeMillis();
        Long elapsed = getElapsedTime();
        logger.debug("⏱️ Timer de transacción detenido - Elapsed: {}ms", elapsed);
    }

    public Long getElapsedTime() {
        if (transactionStartTime == null) {
            return null;
        }

        long endTime = transactionEndTime != null ? transactionEndTime : System.currentTimeMillis();
        return endTime - transactionStartTime;
    }

    // ============================================================================
    // GESTIÓN DE RESPUESTAS MÚLTIPLES
    // ============================================================================

    public void clearMultipleResponses() {
        multipleResponses.clear();
        logger.debug("🧹 Lista de respuestas múltiples limpiada");
    }

    public void addResponse(TransactionResponse response) {
        multipleResponses.add(response);
        logger.debug("➕ Respuesta agregada a lista - Total: {}", multipleResponses.size());
    }

    // ============================================================================
    // ACCESO A SERVICIOS
    // ============================================================================

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    // ============================================================================
    // HELPERS DE VALIDACIÓN
    // ============================================================================

    public void assertLastResponseExists() {
        if (lastResponse == null) {
            logger.error("❌ No hay respuesta REST disponible");
            throw new IllegalStateException("No hay respuesta disponible. ¿Se ejecutó la acción primero?");
        }
        logger.debug("✅ LastResponse existe - Status: {}", lastResponse.getStatusCode());
    }

    public String getResponseField(String fieldPath) {
        assertLastResponseExists();
        String value = lastResponse.jsonPath().getString(fieldPath);
        logger.debug("📄 Campo '{}': {}", fieldPath, value);
        return value;
    }

    public Integer getResponseFieldAsInt(String fieldPath) {
        assertLastResponseExists();
        Integer value = lastResponse.jsonPath().getInt(fieldPath);
        logger.debug("📄 Campo '{}': {}", fieldPath, value);
        return value;
    }

    public Boolean getResponseFieldAsBoolean(String fieldPath) {
        assertLastResponseExists();
        Boolean value = lastResponse.jsonPath().getBoolean(fieldPath);
        logger.debug("📄 Campo '{}': {}", fieldPath, value);
        return value;
    }

    // ============================================================================
    // MÉTODOS DE CONVENIENCIA PARA CONEXIÓN
    // ============================================================================

    public void connectIfNeeded() {
        if (!connectionService.isConnected()) {
            logger.info("🔌 Conectando automáticamente al simulador...");
            connectionService.connect();
            connectionInitialized = true;
        }
    }

    public boolean isConnected() {
        return connectionService.isConnected();
    }

    public void disconnect() {
        if (connectionService.isConnected()) {
            logger.info("🔌 Desconectando del simulador...");
            connectionService.disconnect();
            connectionInitialized = false;
        }
    }

    // ============================================================================
    // MÉTODOS DE LIMPIEZA
    // ============================================================================

    public void cleanup() {
        logger.info("🧹 Limpiando TestContext...");

        reset();

        if (connectionInitialized && connectionService.isConnected()) {
            logger.info("🔌 Cerrando conexión...");
            disconnect();
        }

        logger.info("✅ TestContext limpiado completamente");
    }

    // ============================================================================
    // LOGGING Y DEBUGGING
    // ============================================================================

    public void logCurrentState() {
        logger.info("📊 Estado actual del TestContext:");
        logger.info("  - Última respuesta REST: {}", lastResponse != null ? "✅" : "❌");
        logger.info("  - Builder activo: {}", requestBuilder != null ? "✅" : "❌");
        logger.info("  - Request actual: {}", currentRequest != null ? "✅" : "❌");
        logger.info("  - Response actual: {}", currentResponse != null ? "✅" : "❌");
        logger.info("  - Respuestas múltiples: {}", multipleResponses.size());
        logger.info("  - Tipo de transacción: {}", currentTransactionType);
        logger.info("  - Conexión inicializada: {}", connectionInitialized ? "✅" : "❌");
        logger.info("  - Conectado al simulador: {}", isConnected() ? "✅" : "❌");
        logger.info("  - Tiempo transcurrido: {}ms", getElapsedTime());

        if (currentResponse != null) {
            logger.info("  - Response Code: {}", currentResponse.getResponseCode());
            logger.info("  - STAN: {}", currentResponse.getStan());
            logger.info("  - Success: {}", currentResponse.getSuccessful());
        }
    }

}