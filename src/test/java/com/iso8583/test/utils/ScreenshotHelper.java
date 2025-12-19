package com.iso8583.test.utils;

import com.iso8583.test.config.TestContext;
import com.iso8583.test.models.TransactionRequest;
import com.iso8583.test.models.TransactionResponse;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Helper para capturar estado del sistema en failures
 */
public class ScreenshotHelper {

    private static final Logger logger = LoggerFactory.getLogger(ScreenshotHelper.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void captureFailureState(TestContext context, Throwable error) {
        logger.info("📸 Capturando estado del sistema por failure...");

        try {
            captureErrorDetails(error);

            if (context.getCurrentResponse() != null) {
                captureTransactionState(context);
            }

            captureConnectionState(context);
            captureTimestamp();

            logger.info("✅ Estado del sistema capturado exitosamente");
        } catch (Exception e) {
            logger.error("❌ Error capturando estado del sistema: {}", e.getMessage());
        }
    }

    public static void captureSuccessState(TestContext context) {
        if (context.getCurrentResponse() == null) {
            return;
        }

        try {
            String state = buildTransactionStateReport(
                    context.getCurrentRequest(),
                    context.getCurrentResponse(),
                    "SUCCESS"
            );

            Allure.addAttachment("✅ Transacción Exitosa - Estado Completo", "text/plain", state, ".txt");
        } catch (Exception e) {
            logger.warn("⚠️ Error capturando estado exitoso: {}", e.getMessage());
        }
    }

    private static void captureErrorDetails(Throwable error) {
        if (error == null) {
            return;
        }

        StringBuilder errorDetails = new StringBuilder();
        errorDetails.append("❌ ERROR DETAILS\n");
        errorDetails.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        errorDetails.append("Error Type: ").append(error.getClass().getSimpleName()).append("\n");
        errorDetails.append("Error Message: ").append(error.getMessage()).append("\n");
        errorDetails.append("\nStack Trace:\n");

        for (StackTraceElement element : error.getStackTrace()) {
            if (element.getClassName().startsWith("com.iso8583")) {
                errorDetails.append("  at ").append(element.toString()).append("\n");
            }
        }

        if (error.getCause() != null) {
            errorDetails.append("\nCaused by: ").append(error.getCause().getMessage()).append("\n");
        }

        Allure.addAttachment("❌ Error Details", "text/plain", errorDetails.toString(), ".txt");
    }

    private static void captureTransactionState(TestContext context) {
        String state = buildTransactionStateReport(
                context.getCurrentRequest(),
                context.getCurrentResponse(),
                "FAILURE"
        );

        Allure.addAttachment("📸 Transaction State Snapshot", "text/plain", state, ".txt");
    }

    private static void captureConnectionState(TestContext context) {
        StringBuilder connectionState = new StringBuilder();
        connectionState.append("🔌 CONNECTION STATE\n");
        connectionState.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        try {
            var connectionService = context.getConnectionService();
            boolean connected = connectionService.isConnected();

            connectionState.append("Status: ").append(connected ? "✅ CONNECTED" : "❌ DISCONNECTED").append("\n");
            connectionState.append("Simulator URL: http://localhost:8081\n");
            connectionState.append("Simulator Mode: REAL\n");

            Allure.addAttachment("🔌 Connection State", "text/plain", connectionState.toString(), ".txt");
        } catch (Exception e) {
            connectionState.append("Error checking connection: ").append(e.getMessage()).append("\n");
        }
    }

    private static void captureTimestamp() {
        String timestamp = LocalDateTime.now().format(formatter);
        Allure.addAttachment("🕐 Failure Timestamp", timestamp);
    }

    public static void captureManualSnapshot(TestContext context, String snapshotName) {
        logger.info("📸 Capturando snapshot manual: {}", snapshotName);

        StringBuilder snapshot = new StringBuilder();
        snapshot.append("📸 MANUAL SNAPSHOT: ").append(snapshotName).append("\n");
        snapshot.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        snapshot.append("Timestamp: ").append(LocalDateTime.now().format(formatter)).append("\n");

        if (context.getCurrentRequest() != null) {
            snapshot.append("\nCurrent Request Type: ").append(context.getCurrentRequest().getTransactionType()).append("\n");
        }

        if (context.getCurrentResponse() != null) {
            snapshot.append("Current Response Code: ").append(context.getCurrentResponse().getResponseCode()).append("\n");
            snapshot.append("Current Response Success: ").append(context.getCurrentResponse().getSuccessful()).append("\n");
        }

        Allure.addAttachment("📸 " + snapshotName, "text/plain", snapshot.toString(), ".txt");
    }

    @Attachment(value = "📄 {logFileName}", type = "text/plain", fileExtension = ".log")
    public static byte[] attachLogFile(String logFileName, String logContent) {
        return logContent.getBytes(StandardCharsets.UTF_8);
    }

    private static String buildTransactionStateReport(
            TransactionRequest request,
            TransactionResponse response,
            String status) {

        StringBuilder report = new StringBuilder();
        report.append("📸 TRANSACTION STATE SNAPSHOT\n");
        report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        report.append("Status: ").append(status).append("\n");
        report.append("Timestamp: ").append(LocalDateTime.now().format(formatter)).append("\n");
        report.append("\n");

        if (request != null) {
            report.append("📤 REQUEST:\n");
            report.append("  Transaction Type: ").append(request.getTransactionType()).append("\n");
            report.append("  PAN: ").append(maskPan(request.getPan())).append("\n");
            report.append("  Amount: ").append(request.getAmount()).append("\n");
            report.append("  Terminal ID: ").append(request.getTerminalId()).append("\n");
            report.append("  Processing Code: ").append(request.getProcessingCode()).append("\n");
            report.append("\n");
        }

        if (response != null) {
            report.append("📥 RESPONSE:\n");
            report.append("  Success: ").append(response.getSuccessful() ? "✅" : "❌").append("\n");
            report.append("  Response Code: ").append(response.getResponseCode()).append("\n");
            report.append("  Response Message: ").append(response.getResponseMessage()).append("\n");
            report.append("  MTI: ").append(response.getMti()).append("\n");
            report.append("  STAN: ").append(response.getStan()).append("\n");
            report.append("  Response Time: ").append(response.getResponseTime()).append(" ms\n");
            report.append("  HTTP Status: ").append(response.getHttpStatusCode()).append("\n");

            if (response.hasValidationErrors()) {
                report.append("\n❌ VALIDATION ERRORS:\n");
                response.getValidationErrors().forEach(error ->
                        report.append("  • ").append(error).append("\n")
                );
            }

            if (response.hasValidationWarnings()) {
                report.append("\n⚠️ VALIDATION WARNINGS:\n");
                response.getValidationWarnings().forEach(warning ->
                        report.append("  • ").append(warning).append("\n")
                );
            }

            if (response.getIsoFields() != null && !response.getIsoFields().isEmpty()) {
                report.append("\n📋 ISO8583 FIELDS:\n");
                response.getIsoFields().forEach((field, value) ->
                        report.append("  Field ").append(field).append(": ").append(value).append("\n")
                );
            }
        }

        report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        return report.toString();
    }

    private static String maskPan(String pan) {
        if (pan == null || pan.length() < 10) {
            return "INVALID_PAN";
        }
        return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);
    }
}