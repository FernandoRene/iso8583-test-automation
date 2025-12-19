package com.iso8583.test.client;

import com.iso8583.test.config.ConfigurationManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

/**
 * Cliente REST para el simulador ISO8583
 * Encapsula todas las llamadas HTTP usando RestAssured
 * VERSIÓN SIN SPRING BOOT
 */
public class ISO8583ApiClient {

    private final ConfigurationManager configManager;
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public ISO8583ApiClient(ConfigurationManager configManager) {
        this.configManager = configManager;

        // Configurar RestAssured
        RestAssured.baseURI = configManager.getBaseUrl();

        // Request spec común
        this.requestSpec = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().ifValidationFails();

        // Response spec común (sin validaciones estrictas)
        this.responseSpec = RestAssured.expect()
                .log().ifValidationFails();

        System.out.println("✅ ISO8583ApiClient inicializado - Base URL: " + configManager.getBaseUrl());
    }

    // ============================================================================
    // MÉTODOS DE CONEXIÓN
    // ============================================================================

    /**
     * Establece conexión con el simulador
     * @return Response con el resultado de la conexión
     */
    public Response connect() {
        System.out.println("🔌 POST /api/v1/connection/connect");

        return given()
                .spec(requestSpec)
                .when()
                .post("/api/v1/connection/connect")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Desconecta del simulador
     * @return Response con el resultado de la desconexión
     */
    public Response disconnect() {
        System.out.println("🔌 POST /api/v1/connection/disconnect");

        return given()
                .spec(requestSpec)
                .when()
                .post("/api/v1/connection/disconnect")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Obtiene el estado de la conexión
     * @return Response con el estado actual
     */
    public Response getConnectionStatus() {
        System.out.println("📊 GET /api/v1/connection/status");

        return given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/connection/status")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Prueba la conexión con un network test message (MTI 0800)
     * @return Response con el resultado del test
     */
    public Response testConnection() {
        System.out.println("🔍 POST /api/v1/connection/test");

        return given()
                .spec(requestSpec)
                .when()
                .post("/api/v1/connection/test")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Limpia el buffer de respuestas pendientes
     * @return Response con el resultado de la limpieza
     */
    public Response clearResponseBuffer() {
        System.out.println("🧹 POST /api/v1/connection/clear-buffer");

        return given()
                .spec(requestSpec)
                .when()
                .post("/api/v1/connection/clear-buffer")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Configura el keep-alive
     * @param intervalMinutes Intervalo en minutos para el keep-alive
     * @return Response con el resultado de la configuración
     */
    public Response configureKeepAlive(int intervalMinutes) {
        System.out.println("⏰ POST /api/v1/connection/keep-alive/enable?intervalMinutes=" + intervalMinutes);

        return given()
                .spec(requestSpec)
                .queryParam("intervalMinutes", intervalMinutes)
                .when()
                .post("/api/v1/connection/keep-alive/enable")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Deshabilita el keep-alive
     * @return Response con el resultado
     */
    public Response disableKeepAlive() {
        System.out.println("🚫 POST /api/v1/connection/keep-alive/disable");

        return given()
                .spec(requestSpec)
                .when()
                .post("/api/v1/connection/keep-alive/disable")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    // ============================================================================
    // MÉTODOS DE TRANSACCIONES
    // ============================================================================

    /**
     * Envía una transacción de Balance Inquiry
     * @param request Request de la transacción
     * @return Response con el resultado
     */
    public Response sendBalanceInquiry(Object request) {
        System.out.println("💰 POST /api/v1/transactions/balance-inquiry");

        String baseUrl = "http://localhost:8081"; // Ajusta según tu contexto

        // Debug: verificar el request antes de enviar
        System.out.println("🔍 Request body: " + request.toString());
        return given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/transactions/balance-inquiry")
                .then()
                .statusCode(200) // Espera 200 OK
                .extract()
                .response();
    }

    /**
     * Envía una transacción de Cash Advance
     * @param request Request de la transacción
     * @return Response con el resultado
     */
    public Response sendCashAdvance(Object request) {
        System.out.println("💵 POST /api/v1/transactions/cash-advance");

        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/transactions/cash-advance")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Envía una transacción de Purchase
     * @param request Request de la transacción
     * @return Response con el resultado
     */
    public Response sendPurchase(Object request) {
        System.out.println("🛒 POST /api/v1/transactions/purchase");

        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/transactions/purchase")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Envía una transacción de Transfer
     * @param request Request de la transacción
     * @return Response con el resultado
     */
    public Response sendTransfer(Object request) {
        System.out.println("💸 POST /api/v1/transactions/transfer");

        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/transactions/transfer")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Envía una transacción de Authorization
     * @param request Request de la transacción
     * @return Response con el resultado
     */
    public Response sendAuthorization(Object request) {
        System.out.println("✅ POST /api/v1/transactions/authorization");

        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/transactions/authorization")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Envía una transacción de Deposit
     * @param request Request de la transacción
     * @return Response con el resultado
     */
    public Response sendDeposit(Object request) {
        System.out.println("💰 POST /api/v1/transactions/deposit");

        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/transactions/deposit")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    /**
     * Envía una transacción de Cashback
     * @param request Request de la transacción
     * @return Response con el resultado
     */
    public Response sendCashback(Object request) {
        System.out.println("💵 POST /api/v1/transactions/cashback");

        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/transactions/cashback")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }


    /**
     * Envía una transacción genérica al endpoint /process
     * @param request Request de la transacción
     * @return Response con el resultado
     */
    public Response sendTransaction(Object request) {
        System.out.println("📤 POST /api/v1/transactions/process");

        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/transactions/process")
                .then()
                .spec(responseSpec)
                .extract()
                .response();
    }

    // ============================================================================
    // MÉTODOS DE VALIDACIÓN (Opcionales)
    // ============================================================================

    /**
     * Verifica que el simulador está disponible
     * @return true si está disponible, false en caso contrario
     */
    public boolean isSimulatorAvailable() {
        try {
            Response response = getConnectionStatus();
            return response.getStatusCode() == 200 || response.getStatusCode() == 400;
        } catch (Exception e) {
            System.err.println("⚠️ Simulador no disponible: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la URL base configurada
     * @return URL base del simulador
     */
    public String getBaseUrl() {
        return configManager.getBaseUrl();
    }
}