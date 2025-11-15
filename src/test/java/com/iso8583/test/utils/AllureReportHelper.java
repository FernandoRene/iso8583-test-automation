package com.iso8583.test.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iso8583.test.models.TransactionRequest;
import com.iso8583.test.models.TransactionResponse;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper para generar attachments automáticos en reportes Allure
 * Centraliza la lógica de formateo y adjuntos para consistencia en reportes
 */
public class AllureReportHelper {

    private static final Logger logger = LoggerFactory.getLogger(AllureReportHelper.class);
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    // ============================================================================
    // ATTACHMENTS DE REQUEST
    // ============================================================================

    /**
     * Adjunta el request completo al reporte Allure
     */
    public static void attachRequest(TransactionRequest request) {
        if (request == null) {
            logger.warn("⚠️ Request null, no se puede adjuntar");
            return;
        }

        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("transactionType", request.getTransactionType());
            requestData.put("pan", maskPan(request.getPan()));
            requestData.put("track2", maskTrack2(request.getTrack2()));
            requestData.put("amount", request.getAmount());
            requestData.put("terminalId", request.getTerminalId());
            requestData.put("cardAcceptorId", request.getCardAcceptorId());
            requestData.put("cardAcceptorName", request.getCardAcceptorName());
            requestData.put("currencyCode", request.getCurrencyCode());
            requestData.put("processingCode", request.getProcessingCode());
            requestData.put("account", request.getAccount());
            requestData.put("targetAccount", request.getTargetAccount());

            String json = gson.toJson(requestData);
            Allure.addAttachment("📤 Request", "application/json", json, ".json");

            logger.debug("✅ Request adjuntado a Allure");

        } catch (Exception e) {
            logger.error("❌ Error adjuntando request: {}", e.getMessage());
        }
    }

    /**
     * Adjunta solo el request summary (versión compacta)
     */
    public static void attachRequestSummary(TransactionRequest request) {
        if (request == null) {
            return;
        }

        String summary = String.format(
                "Type: %s\nPAN: %s\nAmount: %s\nTerminal: %s",
                request.getTransactionType(),
                maskPan(request.getPan()),
                request.getAmount(),
                request.getTerminalId()
        );

        Allure.addAttachment("📋 Request Summary", "text/plain", summary, ".txt");
    }

    // ============================================================================
    // ATTACHMENTS DE RESPONSE
    // ============================================================================

    /**
     * Adjunta la respuesta completa al reporte Allure
     */
    public static void attachResponse(TransactionResponse response) {
        if (response == null) {
            logger.warn("⚠️ Response null, no se puede adjuntar");
            return;
        }

        try {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("successful", response.getSuccessful());
            responseData.put("responseCode", response.getResponseCode());
            responseData.put("responseMessage", response.getResponseMessage());
            responseData.put("mti", response.getMti());
            responseData.put("stan", response.getStan());
            responseData.put("responseTime", response.getResponseTime());
            responseData.put("httpStatusCode", response.getHttpStatusCode());
            responseData.put("validationErrors", response.getValidationErrors());
            responseData.put("validationWarnings", response.getValidationWarnings());

            String json = gson.toJson(responseData);
            Allure.addAttachment("📥 Response", "application/json", json, ".json");

            logger.debug("✅ Response adjuntada a Allure");

        } catch (Exception e) {
            logger.error("❌ Error adjuntando response: {}", e.getMessage());
        }
    }

    /**
     * Adjunta solo el response summary (versión compacta)
     */
    public static void attachResponseSummary(TransactionResponse response) {
        if (response == null) {
            return;
        }

        String summary = String.format(
                "Success: %s\nCode: %s\nMessage: %s\nSTAN: %s\nTime: %dms",
                response.getSuccessful() ? "✅" : "❌",
                response.getResponseCode(),
                response.getResponseMessage(),
                response.getStan(),
                response.getResponseTime()
        );

        Allure.addAttachment("📋 Response Summary", "text/plain", summary, ".txt");
    }

    /**
     * Adjunta la respuesta HTTP raw de RestAssured
     */
    public static void attachRawHttpResponse(Response response) {
        if (response == null) {
            return;
        }

        try {
            String rawResponse = String.format(
                    "Status Code: %d\nStatus Line: %s\n\nBody:\n%s",
                    response.getStatusCode(),
                    response.getStatusLine(),
                    response.getBody().asPrettyString()
            );

            Allure.addAttachment("🔍 HTTP Response (Raw)", "text/plain", rawResponse, ".txt");

        } catch (Exception e) {
            logger.error("❌ Error adjuntando raw response: {}", e.getMessage());
        }
    }

    // ============================================================================
    // ATTACHMENTS DE MÉTRICAS
    // ============================================================================

    /**
     * Adjunta métricas de performance
     */
    public static void attachMetrics(TransactionResponse response) {
        if (response == null) {
            return;
        }

        String metrics = String.format(
                "📊 MÉTRICAS DE PERFORMANCE\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "HTTP Status:      %d\n" +
                        "Response Time:    %d ms\n" +
                        "Success:          %s\n" +
                        "STAN:             %s\n" +
                        "Response Code:    %s\n" +
                        "MTI:              %s\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                response.getHttpStatusCode(),
                response.getResponseTime(),
                response.getSuccessful() ? "✅ YES" : "❌ NO",
                response.getStan(),
                response.getResponseCode(),
                response.getMti()
        );

        Allure.addAttachment("📊 Metrics", "text/plain", metrics, ".txt");
    }

    /**
     * Adjunta métricas de múltiples transacciones
     */
    public static void attachBatchMetrics(java.util.List<TransactionResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }

        long totalTime = responses.stream()
                .mapToLong(r -> r.getResponseTime() != null ? r.getResponseTime() : 0L)
                .sum();

        long avgTime = totalTime / responses.size();

        long minTime = responses.stream()
                .mapToLong(r -> r.getResponseTime() != null ? r.getResponseTime() : 0L)
                .min()
                .orElse(0);

        long maxTime = responses.stream()
                .mapToLong(r -> r.getResponseTime() != null ? r.getResponseTime() : 0L)
                .max()
                .orElse(0);

        long successCount = responses.stream()
                .filter(TransactionResponse::getSuccessful)
                .count();

        String metrics = String.format(
                "📊 MÉTRICAS DE LOTE\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Total Transactions:   %d\n" +
                        "Successful:           %d (%.2f%%)\n" +
                        "Failed:               %d\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Avg Response Time:    %d ms\n" +
                        "Min Response Time:    %d ms\n" +
                        "Max Response Time:    %d ms\n" +
                        "Total Time:           %d ms\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                responses.size(),
                successCount,
                (successCount * 100.0 / responses.size()),
                (responses.size() - successCount),
                avgTime,
                minTime,
                maxTime,
                totalTime
        );

        Allure.addAttachment("📊 Batch Metrics", "text/plain", metrics, ".txt");
    }

    // ============================================================================
    // ATTACHMENTS DE ERROR
    // ============================================================================

    /**
     * Adjunta contexto de error detallado
     */
    public static void attachErrorContext(TransactionRequest request, TransactionResponse response) {
        if (response == null) {
            return;
        }

        StringBuilder errorContext = new StringBuilder();
        errorContext.append("❌ ERROR CONTEXT\n");
        errorContext.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        if (request != null) {
            errorContext.append("Transaction Type: ").append(request.getTransactionType()).append("\n");
            errorContext.append("PAN: ").append(maskPan(request.getPan())).append("\n");
            errorContext.append("Amount: ").append(request.getAmount()).append("\n");
            errorContext.append("Terminal: ").append(request.getTerminalId()).append("\n");
            errorContext.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        }

        errorContext.append("Error Code: ").append(response.getResponseCode()).append("\n");
        errorContext.append("Error Message: ").append(response.getResponseMessage()).append("\n");

        if (response.getErrorType() != null) {
            errorContext.append("Error Type: ").append(response.getErrorType()).append("\n");
        }

        if (response.hasValidationErrors()) {
            errorContext.append("Validation Errors:\n");
            response.getValidationErrors().forEach(error ->
                    errorContext.append("  • ").append(error).append("\n")
            );
        }

        if (response.hasValidationWarnings()) {
            errorContext.append("Validation Warnings:\n");
            response.getValidationWarnings().forEach(warning ->
                    errorContext.append("  ⚠️ ").append(warning).append("\n")
            );
        }

        Allure.addAttachment("❌ Error Context", "text/plain", errorContext.toString(), ".txt");
    }

    /**
     * Adjunta validaciones fallidas
     */
    public static void attachValidationErrors(TransactionResponse response) {
        if (response == null || !response.hasValidationErrors()) {
            return;
        }

        StringBuilder errors = new StringBuilder();
        errors.append("❌ VALIDATION ERRORS\n");
        errors.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        response.getValidationErrors().forEach(error ->
                errors.append("• ").append(error).append("\n")
        );

        Allure.addAttachment("❌ Validation Errors", "text/plain", errors.toString(), ".txt");
    }

    // ============================================================================
    // ATTACHMENTS COMPLETOS (ALL-IN-ONE)
    // ============================================================================

    /**
     * Adjunta TODOS los datos de una transacción (request + response + metrics)
     */
    public static void attachCompleteTransaction(TransactionRequest request, TransactionResponse response) {
        attachRequest(request);
        attachResponse(response);
        attachMetrics(response);

        if (!response.getSuccessful()) {
            attachErrorContext(request, response);
        }

        logger.debug("✅ Transacción completa adjuntada a Allure");
    }

    /**
     * Adjunta TODOS los datos en formato compacto
     */
    public static void attachCompactTransaction(TransactionRequest request, TransactionResponse response) {
        attachRequestSummary(request);
        attachResponseSummary(response);

        logger.debug("✅ Transacción compacta adjuntada a Allure");
    }

    // ============================================================================
    // MÉTODOS UTILITARIOS
    // ============================================================================

    /**
     * Enmascara el PAN para mostrar solo primeros 6 y últimos 4 dígitos
     */
    private static String maskPan(String pan) {
        if (pan == null || pan.length() < 10) {
            return "INVALID_PAN";
        }
        return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);
    }

    /**
     * Enmascara el Track2 para mostrar solo primeros 6 dígitos
     */
    private static String maskTrack2(String track2) {
        if (track2 == null || track2.length() < 10) {
            return "INVALID_TRACK2";
        }
        return track2.substring(0, 6) + "***MASKED***";
    }

    /**
     * Attachment annotation para métodos que retornan String
     */
    @Attachment(value = "{name}", type = "text/plain")
    public static String attachText(String name, String content) {
        return content;
    }

    /**
     * Attachment annotation para JSON
     */
    @Attachment(value = "{name}", type = "application/json")
    public static String attachJson(String name, Object object) {
        return gson.toJson(object);
    }
}