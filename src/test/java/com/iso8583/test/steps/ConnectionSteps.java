package com.iso8583.test.steps;

import com.iso8583.test.config.TestContext;
import com.iso8583.test.config.TestContextFactory;
import com.iso8583.test.services.ConnectionService;
import io.cucumber.java.es.*;
import io.qameta.allure.Step;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Step Definitions para gestión de conexiones con el simulador ISO8583
 * ✅ VERSIÓN CORREGIDA: Valida estado REAL de la conexión
 */
public class ConnectionSteps {

    private final TestContext context;

    /**
     * Constructor - Cucumber inyecta TestContextFactory automáticamente
     */
    public ConnectionSteps() {
        this.context = TestContextFactory.getInstance().getTestContext();
    }

    // ============================================================================
    // GIVEN STEPS - PRECONDICIONES DE CONEXIÓN
    // ============================================================================

    @Dado("que el simulador ISO8583 está disponible en {string}")
    @Step("Verificar disponibilidad del simulador en: {baseUrl}")
    public void verificarSimuladorDisponible(String baseUrl) {
        System.out.println("🔍 Verificando disponibilidad del simulador en: " + baseUrl);

        context.getConnectionService().setBaseUrl(baseUrl);

        boolean isAvailable = context.getConnectionService().isSimulatorAvailable();

        if (!isAvailable) {
            System.err.println("⚠️ Simulador puede no estar disponible en: " + baseUrl);
        } else {
            System.out.println("✅ Simulador disponible en: " + baseUrl);
        }
    }

    @Dado("el servicio está en modo {string} conectado al autorizador")
    @Step("Configurar servicio en modo: {mode}")
    public void configurarModoServicio(String mode) {
        System.out.println("⚙️ Configurando servicio en modo: " + mode);

        // ✅ FIX: setSimulatorMode ya verifica si el modo es el mismo y lo omite
        context.getConnectionService().setSimulatorMode(mode);

        System.out.println("✅ Modo configurado: " + mode);
    }

    @Dado("la conexión con el autorizador está establecida")
    @Step("Establecer conexión con autorizador")
    public void establecerConexion() throws Exception {
        System.out.println("🔗 Estableciendo conexión con el autorizador...");

        // 1. Asegurar conexión básica
        context.ensureConnection();

        // ✅ FIX 2: Validar estado REAL de la conexión
        ConnectionService.ConnectionStatus status = context.getConnectionService().getStatus();

        boolean isConnected = status.isConnected();
        boolean channelConnected = status.isChannelConnected();

        System.out.println("📊 Estado de conexión:");
        System.out.println("   - Simulador Connected: " + isConnected);
        System.out.println("   - Channel Connected: " + channelConnected);
        System.out.println("   - Socket: " + status.getSocketInfo());

        // ✅ FIX 3: Validar ambos estados antes de continuar
        if (!status.isFullyConnected()) {
            // Intentar reconectar una vez más
            System.out.println("⚠️ Conexión no completada, intentando reconectar...");
            Thread.sleep(1000);

            context.getConnectionService().verifyAndReconnect();

            // Verificar nuevamente
            status = context.getConnectionService().getStatus();

            if (!status.isFullyConnected()) {
                throw new RuntimeException(String.format(
                        "❌ Conexión no establecida correctamente - Connected: %s, Channel: %s",
                        status.isConnected(), status.isChannelConnected()
                ));
            }
        }

        System.out.println("✅ Conexión verificada y completamente activa");
    }

    // ============================================================================
    // GIVEN STEPS - SIMULACIÓN DE PROBLEMAS DE CONEXIÓN
    // ============================================================================

    @Dado("se pierde la conexión con el autorizador")
    @Step("Simular pérdida de conexión")
    public void simularPerdidaConexion() {
        System.out.println("❌ Simulando pérdida de conexión...");
        context.disconnect();

        boolean isConnected = context.isConnected();

        assertThat(isConnected)
                .as("Conexión debe estar desconectada")
                .isFalse();

        System.out.println("✅ Conexión perdida exitosamente (simulado)");
    }

    @Dado("el autorizador está configurado para no responder")
    @Step("Configurar autorizador sin respuesta")
    public void configurarAutorizadorSinRespuesta() {
        System.out.println("⏰ Configurando autorizador para no responder...");
        context.getConnectionService().setNoResponseMode(true);
        System.out.println("✅ Autorizador configurado para no responder");
    }

    // ============================================================================
    // WHEN STEPS - ACCIONES DE CONEXIÓN
    // ============================================================================

    @Cuando("se restablece la conexión automáticamente")
    @Step("Restablecer conexión automáticamente")
    public void restablecerConexionAutomaticamente() {
        System.out.println("🔄 Restableciendo conexión automáticamente...");
        context.connectIfNeeded();

        boolean isConnected = context.isConnected();

        assertThat(isConnected)
                .as("Conexión debe estar restablecida")
                .isTrue();

        System.out.println("✅ Conexión restablecida exitosamente");
    }

    @Cuando("conecto al simulador")
    @Step("Conectar al simulador")
    public void conectarAlSimulador() {
        System.out.println("🔌 Conectando al simulador...");
        context.getConnectionService().connect();

        boolean isConnected = context.isConnected();

        assertThat(isConnected)
                .as("Debe estar conectado al simulador")
                .isTrue();

        System.out.println("✅ Conectado al simulador");
    }

    @Cuando("desconecto del simulador")
    @Step("Desconectar del simulador")
    public void desconectarDelSimulador() {
        System.out.println("🔌 Desconectando del simulador...");
        context.disconnect();

        boolean isConnected = context.isConnected();

        assertThat(isConnected)
                .as("Debe estar desconectado del simulador")
                .isFalse();

        System.out.println("✅ Desconectado del simulador");
    }

    // ============================================================================
    // THEN STEPS - VALIDACIONES DE CONEXIÓN
    // ============================================================================

    @Entonces("la conexión debe estar establecida")
    @Step("Validar conexión establecida")
    public void validarConexionEstablecida() {
        boolean isConnected = context.isConnected();

        assertThat(isConnected)
                .as("La conexión debe estar establecida")
                .isTrue();

        System.out.println("✅ Conexión verificada como establecida");
    }

    @Entonces("la conexión debe estar cerrada")
    @Step("Validar conexión cerrada")
    public void validarConexionCerrada() {
        boolean isConnected = context.isConnected();

        assertThat(isConnected)
                .as("La conexión debe estar cerrada")
                .isFalse();

        System.out.println("✅ Conexión verificada como cerrada");
    }

    @Entonces("el estado de la conexión debe ser {string}")
    @Step("Validar estado de conexión: {expectedState}")
    public void validarEstadoConexion(String expectedState) {
        String actualState = context.getConnectionService().getConnectionState();

        assertThat(actualState)
                .as("Estado de la conexión")
                .isEqualToIgnoringCase(expectedState);

        System.out.println("✅ Estado de conexión validado: " + actualState);
    }
}