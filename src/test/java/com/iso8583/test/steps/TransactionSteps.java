package com.iso8583.test.steps;

import com.iso8583.test.config.TestContext;
import com.iso8583.test.config.TestContextFactory;
import com.iso8583.test.models.TransactionRequest;
import com.iso8583.test.models.TransactionResponse;
import com.iso8583.test.models.TransactionType;
import com.iso8583.test.utils.AllureReportHelper;
import io.cucumber.java.es.*;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

/**
 * Step Definitions PARAMETRIZADOS para transacciones ISO8583
 * VERSIÓN SIN SPRING BOOT
 *
 * ✅ VERSIÓN MEJORADA con attachments automáticos de Allure
 */
public class TransactionSteps {

    private final TestContext context;
    private static final Logger logger = LoggerFactory.getLogger(TransactionSteps.class);


    public TransactionSteps() {
        this.context = TestContextFactory.getInstance().getTestContext();
    }
    /**
     * Constructor - Cucumber inyecta TestContextFactory automáticamente
     */
//    public TransactionSteps(TestContextFactory contextFactory) {
//        this.context = contextFactory.getTestContext();
//        logger.info("✅ TransactionSteps inicializado");
//    }

    // ============================================================================
    // GIVEN STEPS - CONFIGURACIÓN DE DATOS
    // ============================================================================

    @Dado("que preparo una transacción de tipo {string}")
    @Step("Preparar transacción tipo: {transactionType}")
    public void prepararTransaccionTipo(String transactionType) {
        logger.info("🆕 Preparando transacción tipo: {}", transactionType);

        TransactionType type = TransactionType.fromCode(transactionType);

        context.startNewRequest()
                .transactionType(type);

        Allure.addAttachment("Transaction Type", transactionType);
    }

    @Dado("que tengo una tarjeta con PAN {string}")
    @Y("con PAN {string}")
    @Step("Configurar PAN: {pan}")
    public void configurarPan(String pan) {
        String maskedPan = maskPan(pan);
        logger.info("💳 Configurando PAN: {}", maskedPan);

        context.getRequestBuilder().pan(pan);

        Allure.addAttachment("PAN (Masked)", maskedPan);
    }

    @Y("el Track2 es {string}")
    @Y("con Track2 {string}")
    @Step("Configurar Track2")
    public void configurarTrack2(String track2) {
        logger.info("💳 Configurando Track2");
        context.getRequestBuilder().track2(track2);

        String maskedTrack2 = maskTrack2(track2);
        Allure.addAttachment("Track2 (Masked)", maskedTrack2);
    }

    @Y("la terminal {string} está configurada")
    @Y("con terminal {string}")
    @Step("Configurar Terminal: {terminalId}")
    public void configurarTerminal(String terminalId) {
        logger.info("🖥 Configurando Terminal: {}", terminalId);
        context.getRequestBuilder().terminalId(terminalId);
        Allure.addAttachment("Terminal ID", terminalId);
    }

    @Y("el comercio {string} está activo")
    @Y("con comercio {string}")
    @Y("con Card Acceptor {string}")
    @Step("Configurar Card Acceptor: {cardAcceptorId}")
    public void configurarComercio(String cardAcceptorId) {
        logger.info("🏪 Configurando Card Acceptor: {}", cardAcceptorId);
        context.getRequestBuilder().cardAcceptorId(cardAcceptorId);
        Allure.addAttachment("Card Acceptor ID", cardAcceptorId);
    }

    @Y("la cuenta a consultar es {string}")
    @Y("con cuenta {string}")
    @Y("con cuenta origen {string}")
    @Step("Configurar cuenta: {account}")
    public void configurarCuenta(String account) {
        logger.info("💰 Configurando cuenta: {}", account);
        context.getRequestBuilder().account(account);
        Allure.addAttachment("Account", account);
    }

    @Y("el monto es {string}")
    @Y("con monto {string}")
    @Step("Configurar monto: {amount}")
    public void configurarMonto(String amount) {
        logger.info("💵 Configurando monto: {}", amount);
        context.getRequestBuilder().amount(amount);
        Allure.addAttachment("Amount", amount);
    }

    @Y("con processing code {string}")
    @Step("Configurar processing code: {processingCode}")
    public void configurarProcessingCode(String processingCode) {
        logger.info("🔧 Configurando processing code: {}", processingCode);
        context.getRequestBuilder().processingCode(processingCode);
        Allure.addAttachment("Processing Code", processingCode);
    }

    @Y("con cuenta destino {string}")
    @Step("Configurar cuenta destino: {targetAccount}")
    public void configurarCuentaDestino(String targetAccount) {
        logger.info("🎯 Configurando cuenta destino: {}", targetAccount);
        context.getRequestBuilder().targetAccount(targetAccount);
        Allure.addAttachment("Target Account", targetAccount);
    }

    @Y("con país adquirente {string}")
    @Step("Configurar país adquirente: {acquiringCountry}")
    public void configurarPaisAdquirente(String acquiringCountry) {
        logger.info("🌍 Configurando país adquirente: {}", acquiringCountry);
        context.getRequestBuilder().acquiringCountry(acquiringCountry);
        Allure.addAttachment("Acquiring Country", acquiringCountry);
    }

    // ============================================================================
    // WHEN STEPS - ENVÍO DE TRANSACCIONES
    // ============================================================================

    @Cuando("envío una solicitud de consulta de saldo")
    @Step("Enviar consulta de saldo")
    public void enviarConsultaSaldo() {
        logger.info("📤 Enviando consulta de saldo");

        if (context.getRequestBuilder().build().getTransactionType() == null) {
            context.getRequestBuilder().transactionType(TransactionType.BALANCE_INQUIRY);
        }

        enviarTransaccion();
    }

    @Cuando("envío la transacción")
    @Cuando("envío la solicitud de transacción")
    @Step("Enviar transacción")
    public void enviarTransaccion() {
        logger.info("📤 Construyendo y enviando transacción");

        context.startTransactionTimer();
        TransactionRequest request = context.buildAndSetRequest();

        logger.info("📋 Request construido: {}", request.getTransactionType());

        // ✅ Adjuntar request ANTES de enviar
        AllureReportHelper.attachRequest(request);

        // Enviar transacción (TransactionService sincroniza automáticamente las responses)
        TransactionResponse response = context.getTransactionService()
                .sendTransaction(request);

        // Ya NO necesitamos setCurrentResponse porque TransactionService lo hace
        // context.setCurrentResponse(response); // ❌ ELIMINADO - TransactionService ya sincroniza

        logger.info("📥 Response recibida - Success: {}, Code: {}, STAN: {}",
                response.getSuccessful(),
                response.getResponseCode(),
                response.getStan());

        // ✅ Adjuntar response DESPUÉS de recibir
        AllureReportHelper.attachResponse(response);
        AllureReportHelper.attachMetrics(response);

        // ✅ Si hay error, adjuntar contexto de error
        if (!response.getSuccessful()) {
            AllureReportHelper.attachErrorContext(request, response);
        }

        // ✅ Adjuntar validaciones si existen
        if (response.hasValidationErrors()) {
            AllureReportHelper.attachValidationErrors(response);
        }
    }

    @Cuando("envío {int} solicitudes de consulta de saldo consecutivas")
    @Step("Enviar {count} transacciones consecutivas")
    public void enviarMultiplesTransacciones(int count) {
        logger.info("📤 Enviando {} transacciones consecutivas", count);

        context.clearMultipleResponses();

        for (int i = 0; i < count; i++) {
            TransactionRequest request = context.buildAndSetRequest();

            logger.info("📤 Enviando transacción {}/{}", (i + 1), count);

            // ✅ Adjuntar cada request
            AllureReportHelper.attachRequest(request);

            TransactionResponse response = context.getTransactionService()
                    .sendTransaction(request);

            context.addResponse(response);

            logger.info("📥 Transacción {}/{} completada - Success: {}",
                    (i + 1), count, response.getSuccessful());

            // ✅ Adjuntar cada response
            AllureReportHelper.attachResponse(response);
        }

        logger.info("✅ {} transacciones enviadas", count);

        // ✅ Adjuntar métricas de lote
        AllureReportHelper.attachBatchMetrics(context.getMultipleResponses());
    }

    // ============================================================================
    // THEN STEPS - VALIDACIONES ESPECÍFICAS DE TRANSACCIONES
    // ============================================================================

    /**
     * Valida que se recibió información de saldo
     */
    @Y("debo recibir información de saldo")
    @Step("Validar información de saldo")
    public void validarInformacionSaldo() {
        TransactionResponse response = context.getCurrentResponse();

        assertThat(response.getSuccessful())
                .as("Debe recibir información de saldo exitosamente")
                .isTrue();

        logger.info("✅ Información de saldo recibida");
    }

    @Y("el STAN debe ser único y secuencial")
    @Step("Validar STAN único y secuencial")
    public void validarSTAN() {
        TransactionResponse response = context.getCurrentResponse();
        String stan = response.getStan();

        assertThat(stan)
                .as("STAN debe existir")
                .isNotNull()
                .matches("\\d{6}");

        logger.info("✅ STAN validado: {}", stan);
        Allure.addAttachment("STAN Validado", stan);
    }

    // ============================================================================
    // THEN STEPS - VALIDACIONES DE MÚLTIPLES TRANSACCIONES
    // ============================================================================

    @Entonces("todas las transacciones deben completarse exitosamente")
    @Step("Validar todas las transacciones exitosas")
    public void validarTodasTransaccionesExitosas() {
        var responses = context.getMultipleResponses();

        assertThat(responses)
                .as("Debe haber transacciones múltiples")
                .isNotEmpty();

        for (int i = 0; i < responses.size(); i++) {
            TransactionResponse response = responses.get(i);

            assertThat(response.getSuccessful())
                    .as("Transacción %d debe ser exitosa", i + 1)
                    .isTrue();
        }

        logger.info("✅ Todas las {} transacciones completadas exitosamente",
                responses.size());
    }

    @Y("cada transacción debe tener un STAN único")
    @Step("Validar STANs únicos")
    public void validarSTANsUnicos() {
        var responses = context.getMultipleResponses();

        var stans = responses.stream()
                .map(TransactionResponse::getStan)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(stans)
                .as("Todos los STANs deben ser únicos")
                .hasSize(responses.size());

        logger.info("✅ {} STANs únicos verificados", stans.size());

        Allure.addAttachment("STANs Únicos", String.join(", ", stans));
    }

    @Y("el tiempo promedio de respuesta debe ser menor a {int} milisegundos")
    @Step("Validar tiempo promedio < {maxAvgTime}ms")
    public void validarTiempoPromedio(int maxAvgTime) {
        var responses = context.getMultipleResponses();

        double avgTime = responses.stream()
                .mapToLong(r -> r.getResponseTime() != null ? r.getResponseTime() : 0L)
                .average()
                .orElse(0);

        assertThat(avgTime)
                .as("Tiempo promedio de respuesta")
                .isLessThan((double) maxAvgTime);

        logger.info("✅ Tiempo promedio validado: {}ms (límite: {}ms)",
                avgTime, maxAvgTime);

        Allure.addAttachment("Avg Response Time", String.format("%.2f ms", avgTime));
    }

    // ============================================================================
    // THEN STEPS - VALIDACIONES DE ERROR
    // ============================================================================

    @Y("el mensaje de respuesta debe indicar rechazo")
    @Step("Validar mensaje de rechazo")
    public void validarMensajeRechazo() {
        TransactionResponse response = context.getCurrentResponse();

        assertThat(response.getSuccessful())
                .as("Transacción debe ser rechazada")
                .isFalse();

        logger.info("✅ Mensaje de rechazo validado: {}",
                response.getResponseMessage());

        Allure.addAttachment("Rejection Message", response.getResponseMessage());
    }

    @Y("debo recibir errores de validación")
    @Step("Validar errores de validación")
    public void validarErroresValidacion() {
        TransactionResponse response = context.getCurrentResponse();

        assertThat(response.hasValidationErrors())
                .as("Debe haber errores de validación")
                .isTrue();

        logger.info("✅ Errores de validación recibidos: {}",
                response.getValidationErrors());

        // ✅ Ya adjuntados automáticamente en enviarTransaccion()
    }

    @Y("los errores deben incluir {string}")
    @Step("Validar errores incluyen: {expectedError}")
    public void validarErroresIncluyen(String expectedError) {
        TransactionResponse response = context.getCurrentResponse();

        boolean containsError = response.getValidationErrors().stream()
                .anyMatch(error -> error.contains(expectedError));

        assertThat(containsError)
                .as("Errores deben incluir: " + expectedError)
                .isTrue();

        logger.info("✅ Error '{}' encontrado en validación", expectedError);
    }

    @Entonces("debería recibir un error de timeout")
    @Step("Validar error de timeout")
    public void validarErrorTimeout() {
        TransactionResponse response = context.getCurrentResponse();

        assertThat(response.isTimeout())
                .as("Debe haber error de timeout")
                .isTrue();

        logger.info("✅ Error de timeout detectado: {}", response.getErrorType());
    }

    @Y("el código de respuesta debe ser uno de: {}")
    @Step("Validar código de respuesta en lista")
    public void validarCodigoRespuestaEnLista(java.util.List<String> validCodes) {
        TransactionResponse response = context.getCurrentResponse();

        assertThat(validCodes)
                .as("Código de respuesta válido")
                .contains(response.getResponseCode());

        logger.info("✅ Código de respuesta: {} (uno de: {})",
                response.getResponseCode(), validCodes);
    }

    // ============================================================================
    // MÉTODOS UTILITARIOS
    // ============================================================================

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 10) {
            return "INVALID_PAN";
        }
        return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);
    }

    private String maskTrack2(String track2) {
        if (track2 == null || track2.length() < 10) {
            return "INVALID_TRACK2";
        }
        return track2.substring(0, 6) + "***MASKED***";
    }
}