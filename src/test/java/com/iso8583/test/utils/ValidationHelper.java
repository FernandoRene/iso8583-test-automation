package com.iso8583.test.utils;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

/**
 * Utilidades para validaciones complejas en tests
 */
public class ValidationHelper {

    private static final Logger logger = LoggerFactory.getLogger(ValidationHelper.class);

    /**
     * Valida un campo según el tipo de validación especificado
     */
    public static void validateField(String fieldName, String actualValue,
                                     String expectedValue, String validationType) {

        logger.debug("Validando campo '{}': {} ({}) vs esperado: {}",
                fieldName, actualValue, validationType, expectedValue);

        switch (validationType.toUpperCase()) {
            case "EXACTO":
                validateExact(fieldName, actualValue, expectedValue);
                break;
            case "CONTIENE":
                validateContains(fieldName, actualValue, expectedValue);
                break;
            case "REGEX":
                validateRegex(fieldName, actualValue, expectedValue);
                break;
            case "NUMERICO":
                validateNumeric(fieldName, actualValue);
                break;
            case "LONGITUD":
                validateLength(fieldName, actualValue, Integer.parseInt(expectedValue));
                break;
            case "NO_VACIO":
                validateNotEmpty(fieldName, actualValue);
                break;
            default:
                validateExact(fieldName, actualValue, expectedValue);
        }
    }

    /**
     * Validación exacta
     */
    public static void validateExact(String fieldName, String actualValue, String expectedValue) {
        assertThat(actualValue)
                .as("Campo %s debe ser exactamente %s", fieldName, expectedValue)
                .isEqualTo(expectedValue);

        Allure.step(String.format("✓ Campo '%s' validado (exacto): %s", fieldName, actualValue));
    }

    /**
     * Validación que contiene
     */
    public static void validateContains(String fieldName, String actualValue, String expectedSubstring) {
        assertThat(actualValue)
                .as("Campo %s debe contener %s", fieldName, expectedSubstring)
                .contains(expectedSubstring);

        Allure.step(String.format("✓ Campo '%s' contiene: %s", fieldName, expectedSubstring));
    }

    /**
     * Validación por regex
     */
    public static void validateRegex(String fieldName, String actualValue, String regex) {
        assertThat(actualValue)
                .as("Campo %s debe cumplir patrón %s", fieldName, regex)
                .matches(regex);

        Allure.step(String.format("✓ Campo '%s' cumple patrón regex", fieldName));
    }

    /**
     * Validación numérica
     */
    public static void validateNumeric(String fieldName, String actualValue) {
        assertThat(actualValue)
                .as("Campo %s debe ser numérico", fieldName)
                .matches("\\d+");

        Allure.step(String.format("✓ Campo '%s' es numérico: %s", fieldName, actualValue));
    }

    /**
     * Validación de longitud
     */
    public static void validateLength(String fieldName, String actualValue, int expectedLength) {
        assertThat(actualValue)
                .as("Campo %s debe tener longitud %d", fieldName, expectedLength)
                .hasSize(expectedLength);

        Allure.step(String.format("✓ Campo '%s' tiene longitud correcta: %d",
                fieldName, expectedLength));
    }

    /**
     * Validación no vacío
     */
    public static void validateNotEmpty(String fieldName, String actualValue) {
        assertThat(actualValue)
                .as("Campo %s no debe estar vacío", fieldName)
                .isNotNull()
                .isNotEmpty();

        Allure.step(String.format("✓ Campo '%s' no está vacío", fieldName));
    }

    /**
     * Valida que un PAN tenga formato válido (sin validar Luhn)
     */
    public static void validatePanFormat(String pan) {
        assertThat(pan)
                .as("PAN debe tener formato válido")
                .matches("\\d{13,19}");

        logger.debug("✓ PAN con formato válido: {}...{}",
                pan.substring(0, 6), pan.substring(pan.length()-4));
    }

    /**
     * Valida STAN (6 dígitos)
     */
    public static void validateStan(String stan) {
        assertThat(stan)
                .as("STAN debe ser 6 dígitos")
                .matches("\\d{6}");

        logger.debug("✓ STAN válido: {}", stan);
    }

    /**
     * Valida código de respuesta ISO8583
     */
    public static void validateResponseCode(String responseCode, boolean shouldBeSuccessful) {
        assertThat(responseCode)
                .as("Response code debe tener 2 caracteres")
                .hasSize(2);

        if (shouldBeSuccessful) {
            assertThat(responseCode)
                    .as("Response code debe ser '00' para transacción exitosa")
                    .isEqualTo("00");

            Allure.step("✓ Transacción APROBADA - Response Code: 00");
        } else {
            assertThat(responseCode)
                    .as("Response code debe ser diferente de '00' para transacción rechazada")
                    .isNotEqualTo("00");

            Allure.step(String.format("✓ Transacción RECHAZADA - Response Code: %s",
                    responseCode));
        }
    }

    /**
     * Valida MTI
     */
    public static void validateMTI(String mti, String expectedPrefix) {
        assertThat(mti)
                .as("MTI debe tener 4 dígitos")
                .matches("\\d{4}");

        if (expectedPrefix != null) {
            assertThat(mti)
                    .as("MTI debe empezar con %s", expectedPrefix)
                    .startsWith(expectedPrefix);
        }

        logger.debug("✓ MTI válido: {}", mti);
    }

    /**
     * Valida tiempo de respuesta
     */
    public static void validateResponseTime(long responseTime, long maxTime) {
        assertThat(responseTime)
                .as("Tiempo de respuesta debe ser menor a %dms", maxTime)
                .isLessThan(maxTime);

        Allure.addAttachment("Response Time", responseTime + "ms");
        logger.debug("✓ Tiempo de respuesta: {}ms (límite: {}ms)", responseTime, maxTime);
    }

    /**
     * Valida que un campo ISO8583 exista
     */
    public static void validateISOFieldExists(java.util.Map<String, String> isoFields,
                                              String fieldNumber) {
        assertThat(isoFields)
                .as("Mensaje ISO debe contener campo %s", fieldNumber)
                .containsKey(fieldNumber);

        String value = isoFields.get(fieldNumber);
        logger.debug("✓ Campo ISO {} existe: {}", fieldNumber, value);
    }

    /**
     * Valida campo de Processing Code
     */
    public static void validateProcessingCode(String processingCode, String expectedType) {
        assertThat(processingCode)
                .as("Processing code debe tener 6 dígitos")
                .matches("\\d{6}");

        if (expectedType != null) {
            String typePrefix = processingCode.substring(0, 2);
            assertThat(typePrefix)
                    .as("Processing code debe corresponder al tipo: %s", expectedType)
                    .isIn(getValidProcessingCodePrefixes(expectedType));
        }

        logger.debug("✓ Processing code válido: {}", processingCode);
    }

    private static String[] getValidProcessingCodePrefixes(String transactionType) {
        switch (transactionType.toUpperCase()) {
            case "BALANCE_INQUIRY":
                return new String[]{"30"};
            case "CASH_ADVANCE":
                return new String[]{"01"};
            case "PURCHASE":
                return new String[]{"00"};
            case "TRANSFER":
                return new String[]{"40"};
            default:
                return new String[]{};
        }
    }
}