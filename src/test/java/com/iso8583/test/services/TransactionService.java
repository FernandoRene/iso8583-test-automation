package com.iso8583.test.services;

import com.iso8583.test.client.ISO8583ApiClient;
import com.iso8583.test.config.TestContext;
import com.iso8583.test.models.TransactionRequest;
import com.iso8583.test.models.TransactionResponse;
import com.iso8583.test.utils.AllureReportHelper;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Service para envío de transacciones ISO8583
 * ✅ VERSIÓN CORREGIDA: Captura respuestas completas en Allure
 */
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final ISO8583ApiClient apiClient;
    private TestContext testContext;

    // Constructor sin TestContext
    public TransactionService(ISO8583ApiClient apiClient) {
        this.apiClient = apiClient;
        this.testContext = null;
        logger.info("✅ TransactionService inicializado");
    }

    // Constructor con TestContext
    public TransactionService(ISO8583ApiClient apiClient, TestContext testContext) {
        this.apiClient = apiClient;
        this.testContext = testContext;
        logger.info("✅ TransactionService inicializado con TestContext");
    }

    /**
     * Setter para TestContext (permite inyección posterior)
     */
    public void setTestContext(TestContext testContext) {
        this.testContext = testContext;
        logger.debug("🔗 TestContext vinculado a TransactionService");
    }

    /**
     * Envía una transacción según su tipo
     * ✅ CORREGIDO: Captura respuestas completas con todos los campos ISO8583
     */
    public TransactionResponse sendTransaction(TransactionRequest request) {
        validateRequest(request);

        logger.info("📤 Enviando transacción: {}", request.getTransactionType());
        logger.debug("   PAN: {}, Amount: {}", request.getMaskedPan(), request.getAmount());

        try {
            // Verificar conexión antes de enviar
            if (testContext != null) {
                testContext.ensureConnection();
            } else {
                logger.warn("⚠️ TestContext no disponible - no se puede verificar conexión");
            }

            // 1. Enviar por tipo y obtener Response de RestAssured
            Response restAssuredResponse = sendByType(request);

            logger.info("📥 Respuesta recibida - HTTP Status: {}", restAssuredResponse.getStatusCode());

            // 2. Parsear Response a TransactionResponse
            TransactionResponse transactionResponse = parseResponse(restAssuredResponse, request);

            // ✅ FIX 3: Adjuntar respuesta COMPLETA a Allure (con todos los campos ISO8583)
            adjuntarResponseCompletaAAllure(transactionResponse, restAssuredResponse);

            // 3. Sincronizar ambas responses en el contexto
            if (testContext != null) {
                logger.debug("🔗 Sincronizando responses en TestContext...");
                testContext.setResponses(transactionResponse, restAssuredResponse);
                logger.debug("✅ Ambas responses sincronizadas en contexto");
            } else {
                logger.warn("⚠️ TestContext no disponible - No se sincronizaron responses");
            }

            return transactionResponse;

        } catch (Exception e) {
            logger.error("❌ Error enviando transacción: {}", e.getMessage(), e);

            // Intentar reconectar en caso de error
            if (testContext != null && e.getMessage().contains("conexión")) {
                logger.info("🔄 Intentando reconectar después del error...");
                testContext.getConnectionService().verifyAndReconnect();
            }

            return createErrorResponse(e.getMessage());
        }
    }

    /**
     * ✅ FIX 4: Adjunta respuesta completa a Allure con TODOS los campos ISO8583
     */
    private void adjuntarResponseCompletaAAllure(TransactionResponse response, Response restAssuredResponse) {
        try {
            Map<String, Object> responseCompleta = new HashMap<>();

            // Información básica
            responseCompleta.put("successful", response.getSuccessful());
            responseCompleta.put("responseCode", response.getResponseCode());
            responseCompleta.put("responseMessage", response.getResponseMessage());

            // ✅ FIX: HTTP Status correcto (no null)
            int httpStatus = response.getHttpStatusCode() != null
                    ? response.getHttpStatusCode()
                    : restAssuredResponse.getStatusCode();
            responseCompleta.put("httpStatusCode", httpStatus);

            // Información de timing
            responseCompleta.put("responseTime", response.getResponseTime() + "ms");

            // Información ISO8583
            responseCompleta.put("mti", response.getMti());
            responseCompleta.put("stan", response.getStan());
            responseCompleta.put("rrn", response.getRrn());
            responseCompleta.put("approvalCode", response.getApprovalCode());

            // ✅ CRÍTICO: Incluir TODOS los campos ISO8583
            if (response.getFields() != null && !response.getFields().isEmpty()) {
                responseCompleta.put("iso8583Fields", response.getFields());

                // Log de campos para debugging
                logger.debug("📋 Campos ISO8583 en response: {}", response.getFields().size());
            } else {
                logger.warn("⚠️ No se encontraron campos ISO8583 en la respuesta");
            }

            // Información de errores (si existe)
            if (response.getErrorType() != null) {
                responseCompleta.put("errorType", response.getErrorType());
            }

            if (response.getValidationErrors() != null && !response.getValidationErrors().isEmpty()) {
                responseCompleta.put("validationErrors", response.getValidationErrors());
            }

            if (response.getValidationWarnings() != null && !response.getValidationWarnings().isEmpty()) {
                responseCompleta.put("validationWarnings", response.getValidationWarnings());
            }

            // Adjuntar a Allure
            AllureReportHelper.attachJson("Response Completa", responseCompleta);

            logger.debug("✅ Response completa adjuntada a Allure");

        } catch (Exception e) {
            logger.warn("⚠️ Error adjuntando response completa a Allure: {}", e.getMessage());
        }
    }

    /**
     * Envía la transacción al endpoint específico según el tipo
     */
    private Response sendByType(TransactionRequest request) {
        Map<String, Object> requestBody = buildRequestBody(request);

        switch (request.getTransactionType()) {
            case BALANCE_INQUIRY:
                logger.debug("→ POST /api/v1/transactions/balance-inquiry");
                return apiClient.sendBalanceInquiry(requestBody);

            case CASH_ADVANCE:
                logger.debug("→ POST /api/v1/transactions/cash-advance");
                return apiClient.sendCashAdvance(requestBody);

            case PURCHASE:
                logger.debug("→ POST /api/v1/transactions/purchase");
                return apiClient.sendPurchase(requestBody);

            case TRANSFER:
                logger.debug("→ POST /api/v1/transactions/transfer");
                return apiClient.sendTransfer(requestBody);

            case AUTHORIZATION:
                logger.debug("→ POST /api/v1/transactions/authorization");
                return apiClient.sendAuthorization(requestBody);

            default:
                throw new IllegalArgumentException("Tipo de transacción no soportado: " + request.getTransactionType());
        }
    }

    /**
     * Parsea Response de RestAssured a TransactionResponse
     */
    private TransactionResponse parseResponse(Response response, TransactionRequest request) {
        try {
            TransactionResponse transactionResponse = response.as(TransactionResponse.class);

            // ✅ FIX: Asegurar que el HTTP status esté presente
            if (transactionResponse.getHttpStatusCode() == null) {
                transactionResponse.setHttpStatusCode(response.getStatusCode());
            }

            logger.info("✅ Response parseada - Success: {}, Code: {}, STAN: {}",
                    transactionResponse.getSuccessful(),
                    transactionResponse.getResponseCode(),
                    transactionResponse.getStan());

            return transactionResponse;

        } catch (Exception e) {
            logger.error("❌ Error parseando response: {}", e.getMessage(), e);

            TransactionResponse errorResponse = new TransactionResponse();
            errorResponse.setSuccessful(false);
            errorResponse.setResponseCode("96");
            errorResponse.setResponseMessage("Error parseando respuesta del simulador: " + e.getMessage());
            errorResponse.setErrorType("PARSE_ERROR");
            errorResponse.setHttpStatusCode(response.getStatusCode());

            return errorResponse;
        }
    }

    /**
     * Crea una respuesta de error
     */
    private TransactionResponse createErrorResponse(String errorMessage) {
        TransactionResponse errorResponse = new TransactionResponse();
        errorResponse.setSuccessful(false);
        errorResponse.setResponseCode("96");
        errorResponse.setResponseMessage(errorMessage);
        errorResponse.setErrorType("SYSTEM_ERROR");
        errorResponse.setHttpStatusCode(500);

        logger.debug("❌ Error response creada: {}", errorMessage);

        return errorResponse;
    }

    /**
     * Construye el body del request como Map para envío JSON
     */
    private Map<String, Object> buildRequestBody(TransactionRequest request) {
        Map<String, Object> body = new HashMap<>();

        addIfNotNull(body, "pan", request.getPan());
        addIfNotNull(body, "track2", request.getTrack2());
        addIfNotNull(body, "amount", request.getAmount());
        addIfNotNull(body, "terminalId", request.getTerminalId());
        addIfNotNull(body, "cardAcceptorId", request.getCardAcceptorId());
        addIfNotNull(body, "cardAcceptorName", request.getCardAcceptorName());
        addIfNotNull(body, "currencyCode", request.getCurrencyCode());
        addIfNotNull(body, "processingCode", request.getProcessingCode());
        addIfNotNull(body, "account", request.getAccount());
        addIfNotNull(body, "targetAccount", request.getTargetAccount());
        addIfNotNull(body, "billingAmount", request.getBillingAmount());
        addIfNotNull(body, "billingCurrency", request.getBillingCurrency());
        addIfNotNull(body, "acquiringCountry", request.getAcquiringCountry());
        addIfNotNull(body, "acquiringInstitution", request.getAcquiringInstitution());
        addIfNotNull(body, "merchantType", request.getMerchantType());
        addIfNotNull(body, "posEntryMode", request.getPosEntryMode());
        addIfNotNull(body, "pinData", request.getPinData());
        addIfNotNull(body, "privateUseFields", request.getPrivateUseFields());

        logger.debug("📦 Request body construido con {} campos", body.size());

        return body;
    }

    /**
     * Agrega campo al map solo si no es null
     */
    private void addIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * Valida que el request tenga los campos mínimos necesarios
     */
    private void validateRequest(TransactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request no puede ser null");
        }

        if (request.getTransactionType() == null) {
            throw new IllegalArgumentException("Tipo de transacción es obligatorio");
        }

        if (!request.hasRequiredFields()) {
            throw new IllegalArgumentException("Request no tiene los campos requeridos mínimos");
        }

        logger.debug("✅ Request válido: {}", request.getDescription());
    }

    /**
     * Log detallado de la transacción y respuesta
     */
    public void logTransactionDetails(TransactionRequest request, TransactionResponse response) {
        logger.info("╔═══════════════════════════════════════════════════════════╗");
        logger.info("📋 DETALLE DE TRANSACCIÓN");
        logger.info("╠═══════════════════════════════════════════════════════════╣");
        logger.info("Tipo:         {}", request.getTransactionType());
        logger.info("PAN:          {}", request.getMaskedPan());
        logger.info("Monto:        {}", request.getAmount());
        logger.info("Terminal:     {}", request.getTerminalId());
        logger.info("───────────────────────────────────────────────────────────");
        logger.info("Success:      {}", response.getSuccessful() ? "✅" : "❌");
        logger.info("Code:         {}", response.getResponseCode());
        logger.info("Message:      {}", response.getResponseMessage());
        logger.info("STAN:         {}", response.getStan());
        logger.info("Time:         {}ms", response.getResponseTime());
        logger.info("HTTP Status:  {}", response.getHttpStatusCode());
        logger.info("╚═══════════════════════════════════════════════════════════╝");
    }
}