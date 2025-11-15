package com.iso8583.test.steps;

import com.iso8583.test.config.TestContext;
import com.iso8583.test.config.TestContextFactory;
import com.iso8583.test.models.TransactionResponse;
import com.iso8583.test.utils.AllureReportHelper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.*;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Steps comunes y reutilizables para todas las pruebas
 *
 * ✅ VERSIÓN MEJORADA con attachments automáticos y mejor logging
 */
public class CommonSteps {

    private final TestContext context;
    private static final Logger logger = LoggerFactory.getLogger(CommonSteps.class);

    /**
     * Constructor - Cucumber inyecta TestContextFactory automáticamente
     */
    public CommonSteps() {
        // ✅ Usar Singleton en lugar de inyección
        this.context = TestContextFactory.getInstance().getTestContext();
        logger.info("✅ CommonSteps inicializado con Singleton");
    }

//    public CommonSteps(TestContext testContext) {
//        this.context = testContext;
//        logger.info("✅ CommonSteps inicializado");
//    }
//    public CommonSteps(TestContextFactory contextFactory) {
//        this.context = contextFactory.getTestContext();
//        logger.info("✅ CommonSteps inicializado");
//    }

    // ============================================================================
    // VALIDACIONES DE RESPUESTA HTTP
    // ============================================================================

    /**
     * Valida el código de estado HTTP de la respuesta
     */
    @Entonces("el código de respuesta HTTP debe ser {int}")
    @Step("Validar código HTTP: {statusCode}")
    public void validarCodigoHTTP(int statusCode) {
        logger.info("🔍 Validando código HTTP esperado: {}", statusCode);

        context.assertLastResponseExists();

        int actualStatusCode = context.getLastResponse().getStatusCode();

        assertThat(actualStatusCode)
                .as("Código de respuesta HTTP")
                .isEqualTo(statusCode);

        logger.info("✅ Código HTTP validado: {}", actualStatusCode);

        Allure.addAttachment("HTTP Status Code", String.valueOf(actualStatusCode));
    }

    /**
     * Valida que un campo de la respuesta tenga un valor específico
     */
    @Entonces("el campo {string} debe ser {string}")
    @Step("Validar campo {field} = {expectedValue}")
    public void validarCampo(String field, String expectedValue) {
        logger.info("🔍 Validando campo '{}' = '{}'", field, expectedValue);

        context.assertLastResponseExists();

        String actualValue = context.getResponseField(field);

        assertThat(actualValue)
                .as("Campo '%s'", field)
                .isEqualTo(expectedValue);

        logger.info("✅ Campo '{}' validado: {}", field, actualValue);

        Allure.addAttachment("Field: " + field, actualValue);
    }

    /**
     * Valida que un campo contenga un texto específico
     */
    @Entonces("el campo {string} debe contener {string}")
    @Step("Validar campo {field} contiene {expectedText}")
    public void validarCampoContiene(String field, String expectedText) {
        logger.info("🔍 Validando que campo '{}' contenga '{}'", field, expectedText);

        context.assertLastResponseExists();

        String actualValue = context.getResponseField(field);

        assertThat(actualValue)
                .as("Campo '%s'", field)
                .contains(expectedText);

        logger.info("✅ Campo '{}' contiene: {}", field, expectedText);

        Allure.addAttachment("Field: " + field, actualValue);
    }

    /**
     * Valida que un campo exista en la respuesta
     */
    @Entonces("el campo {string} debe existir")
    @Step("Validar que campo {field} existe")
    public void validarCampoExiste(String field) {
        logger.info("🔍 Validando existencia del campo '{}'", field);

        context.assertLastResponseExists();

        String value = context.getResponseField(field);

        assertThat(value)
                .as("Campo '%s' debe existir", field)
                .isNotNull();

        logger.info("✅ Campo '{}' existe con valor: {}", field, value);

        Allure.addAttachment("Field: " + field, value);
    }

    // ============================================================================
    // VALIDACIONES DE MENSAJE ISO8583 CON DATATABLE
    // ============================================================================

    /**
     * Valida múltiples campos del mensaje ISO8583 usando una tabla de datos
     *
     * Ejemplo de uso en feature:
     * Entonces el mensaje ISO8583 debe cumplir con:
     *   | Campo | Valor Esperado | Tipo   |
     *   | MTI   | 0200          | Exacto |
     *   | 3     | 301099        | Exacto |
     *   | 41    | ATM001LP      | Exacto |
     */
    @Entonces("el mensaje ISO8583 debe cumplir con:")
    @Step("Validar estructura del mensaje ISO8583")
    public void validarMensajeISO8583(DataTable dataTable) {
        logger.info("🔍 Validando estructura del mensaje ISO8583...");

        TransactionResponse response = context.getCurrentResponse();

        // Convertir DataTable a lista de mapas
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        int validatedFields = 0;
        StringBuilder validationSummary = new StringBuilder();
        validationSummary.append("📋 ISO8583 FIELD VALIDATIONS\n");
        validationSummary.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        for (Map<String, String> row : rows) {
            String campo = row.get("Campo");
            String valorEsperado = row.get("Valor Esperado");
            String tipo = row.get("Tipo");

            logger.debug("  Validando campo: {} = {} ({})", campo, valorEsperado, tipo);

            // Obtener el valor actual del campo
            String valorActual = obtenerCampoISO(response, campo);

            // Validar según el tipo
            switch (tipo.toUpperCase()) {
                case "EXACTO":
                    assertThat(valorActual)
                            .as("Campo ISO8583 '%s'", campo)
                            .isEqualTo(valorEsperado);
                    validationSummary.append(String.format("✅ %s = %s (Exacto)\n", campo, valorActual));
                    break;

                case "CONTIENE":
                    assertThat(valorActual)
                            .as("Campo ISO8583 '%s'", campo)
                            .contains(valorEsperado);
                    validationSummary.append(String.format("✅ %s contiene %s\n", campo, valorEsperado));
                    break;

                case "REGEX":
                    assertThat(valorActual)
                            .as("Campo ISO8583 '%s'", campo)
                            .matches(valorEsperado);
                    validationSummary.append(String.format("✅ %s matches %s\n", campo, valorEsperado));
                    break;

                case "NO_VACIO":
                    assertThat(valorActual)
                            .as("Campo ISO8583 '%s'", campo)
                            .isNotEmpty();
                    validationSummary.append(String.format("✅ %s no vacío (%s)\n", campo, valorActual));
                    break;

                default:
                    logger.warn("⚠️ Tipo de validación desconocido: {}", tipo);
                    validationSummary.append(String.format("⚠️ %s - Tipo desconocido: %s\n", campo, tipo));
            }

            validatedFields++;
        }

        validationSummary.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        validationSummary.append(String.format("Total: %d campos validados", validatedFields));

        logger.info("✅ {} campos ISO8583 validados correctamente", validatedFields);

        Allure.addAttachment("ISO8583 Validations", "text/plain",
                validationSummary.toString(), ".txt");
    }

    /**
     * Obtiene el valor de un campo ISO8583 de la respuesta
     */
    private String obtenerCampoISO(TransactionResponse response, String campo) {
        // Mapear campos especiales
        switch (campo.toUpperCase()) {
            case "MTI":
                return response.getMti();

            default:
                // Para campos numéricos (2, 3, 4, etc.)
                try {
                    int fieldNumber = Integer.parseInt(campo);
                    return response.getIsoField(fieldNumber);
                } catch (NumberFormatException e) {
                    logger.error("❌ Campo ISO8583 inválido: {}", campo);
                    return null;
                }
        }
    }

    /**
     * Valida que la respuesta tenga un MTI específico
     */
    @Entonces("la respuesta debe tener MTI {string}")
    @Step("Validar MTI de respuesta: {expectedMti}")
    public void validarMTIRespuesta(String expectedMti) {
        logger.info("🔍 Validando MTI esperado: {}", expectedMti);

        TransactionResponse response = context.getCurrentResponse();

        String actualMti = response.getMti();

        assertThat(actualMti)
                .as("MTI de respuesta")
                .isEqualTo(expectedMti);

        logger.info("✅ MTI de respuesta validado: {}", actualMti);

        Allure.addAttachment("MTI", actualMti);
    }

    /**
     * Valida que un campo específico de la respuesta exista
     */
    @Entonces("el campo {int} de la respuesta debe existir")
    @Step("Validar que campo ISO {fieldNumber} existe")
    public void validarCampoISOExiste(int fieldNumber) {
        logger.info("🔍 Validando existencia del campo ISO8583: {}", fieldNumber);

        TransactionResponse response = context.getCurrentResponse();

        String value = response.getIsoField(fieldNumber);

        assertThat(value)
                .as("Campo ISO8583 %d debe existir", fieldNumber)
                .isNotNull()
                .isNotEmpty();

        logger.info("✅ Campo ISO8583 {} existe: {}", fieldNumber, value);

        Allure.addAttachment("ISO Field " + fieldNumber, value);
    }

    // ============================================================================
    // VALIDACIONES DE TIEMPO
    // ============================================================================

    /**
     * Valida que el tiempo de respuesta sea menor a un valor máximo
     */
    @Entonces("el tiempo de respuesta debe ser menor a {int} milisegundos")
    @Step("Validar tiempo de respuesta < {maxTime}ms")
    public void validarTiempoRespuesta(int maxTime) {
        logger.info("🔍 Validando tiempo de respuesta < {}ms", maxTime);

        TransactionResponse response = context.getCurrentResponse();

        Long responseTime = response.getResponseTime();

        if (responseTime == null) {
            responseTime = context.getElapsedTime();
        }

        assertThat(responseTime)
                .as("Tiempo de respuesta")
                .isLessThan((long) maxTime);

        logger.info("✅ Tiempo de respuesta validado: {}ms (límite: {}ms)",
                responseTime, maxTime);

        Allure.addAttachment("Response Time", responseTime + "ms");
    }

    // ============================================================================
    // HELPERS
    // ============================================================================

    /**
     * Espera un tiempo determinado (útil para debugging o tiempos de espera)
     */
    @Y("espero {int} segundos")
    @Step("Esperar {seconds} segundos")
    public void esperarSegundos(int seconds) throws InterruptedException {
        logger.info("⏳ Esperando {} segundos...", seconds);
        Thread.sleep(seconds * 1000L);
        logger.info("✅ Espera completada");
    }

    // ============================================================================
    // DEBUGGING Y LOGGING
    // ============================================================================

    /**
     * Log del estado actual del contexto (útil para debugging)
     */
    @Y("muestro el estado actual del contexto")
    @Step("Mostrar estado del contexto")
    public void mostrarEstadoContexto() {
        logger.info("📊 Mostrando estado actual del contexto...");
        context.logCurrentState();

        // Crear attachment con el estado
        StringBuilder estado = new StringBuilder();
        estado.append("📊 TEST CONTEXT STATE\n");
        estado.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        estado.append("LastResponse:         ").append(context.getLastResponse() != null ? "✅" : "❌").append("\n");
        estado.append("CurrentRequest:       ").append(context.getCurrentRequest() != null ? "✅" : "❌").append("\n");

        try {
            TransactionResponse response = context.getCurrentResponse();
            estado.append("CurrentResponse:      ✅\n");
            estado.append("  - Success:          ").append(response.getSuccessful()).append("\n");
            estado.append("  - Code:             ").append(response.getResponseCode()).append("\n");
            estado.append("  - STAN:             ").append(response.getStan()).append("\n");
        } catch (IllegalStateException e) {
            estado.append("CurrentResponse:      ❌\n");
        }

        estado.append("ConnectionInitialized: ").append(context.isConnectionInitialized() ? "✅" : "❌").append("\n");
        estado.append("Connected:            ").append(context.isConnected() ? "✅" : "❌").append("\n");
        estado.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Allure.addAttachment("Context State", "text/plain", estado.toString(), ".txt");
    }

    /**
     * Adjunta la respuesta completa al reporte (para debugging)
     */
    @Y("adjunto la respuesta completa al reporte")
    @Step("Adjuntar respuesta completa")
    public void adjuntarRespuestaCompleta() {
        logger.info("📎 Adjuntando respuesta completa al reporte...");

        try {
            TransactionResponse response = context.getCurrentResponse();
            AllureReportHelper.attachResponse(response);
            AllureReportHelper.attachRawHttpResponse(context.getLastResponse());

            logger.info("✅ Respuesta completa adjuntada");
        } catch (Exception e) {
            logger.error("❌ Error adjuntando respuesta: {}", e.getMessage());
        }
    }
}