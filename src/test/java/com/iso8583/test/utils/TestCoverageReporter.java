package com.iso8583.test.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iso8583.test.models.TransactionResponse;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generador de Dashboard de Cobertura para Reportes
 *
 * Genera métricas detalladas de:
 * - Cobertura de escenarios por feature
 * - Distribución de códigos de respuesta
 * - Tiempos de respuesta (promedio, min, max, percentiles)
 * - Tasa de éxito/fallo
 * - Transacciones por tipo
 *
 * ✅ Compatible con Allure y Maven Surefire
 */
public class TestCoverageReporter {

    private static final Logger logger = LoggerFactory.getLogger(TestCoverageReporter.class);
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Almacenamiento de métricas durante la ejecución
    private static final List<TransactionResponse> allResponses = new ArrayList<>();
    private static final Map<String, Integer> scenariosByFeature = new HashMap<>();
    private static final Map<String, Integer> responseCodeDistribution = new HashMap<>();
    private static final Map<String, Integer> transactionTypeCount = new HashMap<>();
    private static final List<Long> responseTimes = new ArrayList<>();

    // ============================================================================
    // REGISTRO DE TRANSACCIONES
    // ============================================================================

    /**
     * Registra una transacción para incluirla en el dashboard
     */
    public static void recordTransaction(TransactionResponse response, String featureName, String transactionType) {
        if (response == null) {
            return;
        }

        synchronized (allResponses) {
            // Almacenar response completa
            allResponses.add(response);

            // Contar por feature
            scenariosByFeature.merge(featureName, 1, Integer::sum);

            // Contar por código de respuesta
            if (response.getResponseCode() != null) {
                responseCodeDistribution.merge(response.getResponseCode(), 1, Integer::sum);
            }

            // Contar por tipo de transacción
            if (transactionType != null) {
                transactionTypeCount.merge(transactionType, 1, Integer::sum);
            }

            // Registrar tiempo de respuesta
            if (response.getResponseTime() != null) {
                responseTimes.add(response.getResponseTime());
            }
        }

        logger.debug("📊 Transacción registrada: {} - {} - Code: {}",
                featureName, transactionType, response.getResponseCode());
    }

    // ============================================================================
    // GENERACIÓN DE REPORTES
    // ============================================================================

    /**
     * Genera el dashboard completo y lo guarda como JSON + adjunta a Allure
     */
    public static void generateDashboard() {
        logger.info("📊 Generando Dashboard de Cobertura...");

        try {
            CoverageReport report = buildCoverageReport();

            // Guardar como JSON
            String jsonPath = saveAsJson(report);
            logger.info("✅ Dashboard guardado en: {}", jsonPath);

            // Adjuntar a Allure
            attachToAllure(report);
            logger.info("✅ Dashboard adjuntado a Allure");

            // Generar resumen en consola
            printConsoleSummary(report);

        } catch (Exception e) {
            logger.error("❌ Error generando dashboard: {}", e.getMessage(), e);
        }
    }

    /**
     * Construye el reporte de cobertura completo
     */
    private static CoverageReport buildCoverageReport() {
        CoverageReport report = new CoverageReport();

        // Información general
        report.timestamp = LocalDateTime.now().format(formatter);
        report.totalTransactions = allResponses.size();
        report.successfulTransactions = countSuccessful();
        report.failedTransactions = allResponses.size() - countSuccessful();
        report.successRate = calculateSuccessRate();

        // Cobertura por feature
        report.scenariosByFeature = new HashMap<>(scenariosByFeature);

        // Distribución de códigos de respuesta
        report.responseCodeDistribution = new HashMap<>(responseCodeDistribution);

        // Distribución por tipo de transacción
        report.transactionTypeDistribution = new HashMap<>(transactionTypeCount);

        // Métricas de performance
        report.performanceMetrics = buildPerformanceMetrics();

        // Top errores
        report.topErrors = buildTopErrors();

        return report;
    }

    /**
     * Construye métricas de performance
     */
    private static PerformanceMetrics buildPerformanceMetrics() {
        if (responseTimes.isEmpty()) {
            return new PerformanceMetrics();
        }

        PerformanceMetrics metrics = new PerformanceMetrics();

        List<Long> sortedTimes = new ArrayList<>(responseTimes);
        Collections.sort(sortedTimes);

        metrics.averageResponseTime = calculateAverage(sortedTimes);
        metrics.minResponseTime = sortedTimes.get(0);
        metrics.maxResponseTime = sortedTimes.get(sortedTimes.size() - 1);
        metrics.medianResponseTime = calculatePercentile(sortedTimes, 50);
        metrics.p90ResponseTime = calculatePercentile(sortedTimes, 90);
        metrics.p95ResponseTime = calculatePercentile(sortedTimes, 95);
        metrics.p99ResponseTime = calculatePercentile(sortedTimes, 99);

        return metrics;
    }

    /**
     * Construye lista de errores más frecuentes
     */
    private static List<ErrorSummary> buildTopErrors() {
        Map<String, Long> errorFrequency = allResponses.stream()
                .filter(r -> !r.getSuccessful())
                .collect(Collectors.groupingBy(
                        r -> r.getResponseCode() + ": " + r.getResponseMessage(),
                        Collectors.counting()
                ));

        return errorFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new ErrorSummary(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());
    }

    /**
     * Guarda el reporte como JSON
     */
    private static String saveAsJson(CoverageReport report) throws IOException {
        Path targetDir = Paths.get("target/test-reports");
        Files.createDirectories(targetDir);

        String filename = "coverage-report-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".json";
        Path jsonPath = targetDir.resolve(filename);

        try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
            gson.toJson(report, writer);
        }

        return jsonPath.toString();
    }

    /**
     * Adjunta el dashboard a Allure en formato legible
     */
    private static void attachToAllure(CoverageReport report) {
        StringBuilder dashboard = new StringBuilder();
        dashboard.append("📊 TEST COVERAGE DASHBOARD\n");
        dashboard.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        dashboard.append("Generated: ").append(report.timestamp).append("\n\n");

        // Resumen general
        dashboard.append("📈 GENERAL SUMMARY\n");
        dashboard.append("─────────────────────────────────────────────────────\n");
        dashboard.append(String.format("Total Transactions:     %d\n", report.totalTransactions));
        dashboard.append(String.format("Successful:             %d (%.2f%%)\n",
                report.successfulTransactions, report.successRate));
        dashboard.append(String.format("Failed:                 %d (%.2f%%)\n\n",
                report.failedTransactions, 100 - report.successRate));

        // Cobertura por feature
        dashboard.append("📋 SCENARIOS BY FEATURE\n");
        dashboard.append("─────────────────────────────────────────────────────\n");
        report.scenariosByFeature.forEach((feature, count) ->
                dashboard.append(String.format("%-30s: %3d scenarios\n", feature, count))
        );
        dashboard.append("\n");

        // Códigos de respuesta
        dashboard.append("📊 RESPONSE CODE DISTRIBUTION\n");
        dashboard.append("─────────────────────────────────────────────────────\n");
        report.responseCodeDistribution.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String emoji = entry.getKey().equals("00") ? "✅" : "❌";
                    dashboard.append(String.format("%s Code %s: %3d (%5.2f%%)\n",
                            emoji,
                            entry.getKey(),
                            entry.getValue(),
                            (entry.getValue() * 100.0 / report.totalTransactions)));
                });
        dashboard.append("\n");

        // Distribución por tipo
        dashboard.append("💳 TRANSACTION TYPE DISTRIBUTION\n");
        dashboard.append("─────────────────────────────────────────────────────\n");
        report.transactionTypeDistribution.forEach((type, count) ->
                dashboard.append(String.format("%-30s: %3d (%5.2f%%)\n",
                        type, count, (count * 100.0 / report.totalTransactions)))
        );
        dashboard.append("\n");

        // Performance
        dashboard.append("⏱️ PERFORMANCE METRICS\n");
        dashboard.append("─────────────────────────────────────────────────────\n");
        dashboard.append(String.format("Average Response Time:  %6d ms\n", report.performanceMetrics.averageResponseTime));
        dashboard.append(String.format("Min Response Time:      %6d ms\n", report.performanceMetrics.minResponseTime));
        dashboard.append(String.format("Max Response Time:      %6d ms\n", report.performanceMetrics.maxResponseTime));
        dashboard.append(String.format("Median (P50):           %6d ms\n", report.performanceMetrics.medianResponseTime));
        dashboard.append(String.format("P90:                    %6d ms\n", report.performanceMetrics.p90ResponseTime));
        dashboard.append(String.format("P95:                    %6d ms\n", report.performanceMetrics.p95ResponseTime));
        dashboard.append(String.format("P99:                    %6d ms\n", report.performanceMetrics.p99ResponseTime));
        dashboard.append("\n");

        // Top errores
        if (!report.topErrors.isEmpty()) {
            dashboard.append("❌ TOP ERRORS\n");
            dashboard.append("─────────────────────────────────────────────────────\n");
            for (int i = 0; i < report.topErrors.size(); i++) {
                ErrorSummary error = report.topErrors.get(i);
                dashboard.append(String.format("%2d. %s (count: %d)\n",
                        i + 1, error.errorMessage, error.count));
            }
        }

        dashboard.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Allure.addAttachment("📊 Coverage Dashboard", "text/plain", dashboard.toString(), ".txt");
    }

    /**
     * Imprime resumen en consola
     */
    private static void printConsoleSummary(CoverageReport report) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("📊 TEST COVERAGE SUMMARY");
        System.out.println("═".repeat(60));
        System.out.printf("Total Tests: %d | Success: %d (%.2f%%) | Failed: %d\n",
                report.totalTransactions,
                report.successfulTransactions,
                report.successRate,
                report.failedTransactions);
        System.out.printf("Avg Response Time: %dms | Max: %dms | P95: %dms\n",
                report.performanceMetrics.averageResponseTime,
                report.performanceMetrics.maxResponseTime,
                report.performanceMetrics.p95ResponseTime);
        System.out.println("═".repeat(60) + "\n");
    }

    // ============================================================================
    // MÉTODOS AUXILIARES
    // ============================================================================

    private static long countSuccessful() {
        return allResponses.stream()
                .filter(TransactionResponse::getSuccessful)
                .count();
    }

    private static double calculateSuccessRate() {
        if (allResponses.isEmpty()) {
            return 0.0;
        }
        return (countSuccessful() * 100.0) / allResponses.size();
    }

    private static long calculateAverage(List<Long> values) {
        return values.isEmpty() ? 0 : (long) values.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    private static long calculatePercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }
        int index = (int) Math.ceil((percentile / 100.0) * sortedValues.size()) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }

    /**
     * Limpia los datos acumulados (útil para tests)
     */
    public static void reset() {
        synchronized (allResponses) {
            allResponses.clear();
            scenariosByFeature.clear();
            responseCodeDistribution.clear();
            transactionTypeCount.clear();
            responseTimes.clear();
        }
    }

    // ============================================================================
    // CLASES INTERNAS - MODELOS DE DATOS
    // ============================================================================

    public static class CoverageReport {
        public String timestamp;
        public int totalTransactions;
        public long successfulTransactions;
        public long failedTransactions;
        public double successRate;
        public Map<String, Integer> scenariosByFeature;
        public Map<String, Integer> responseCodeDistribution;
        public Map<String, Integer> transactionTypeDistribution;
        public PerformanceMetrics performanceMetrics;
        public List<ErrorSummary> topErrors;
    }

    public static class PerformanceMetrics {
        public long averageResponseTime;
        public long minResponseTime;
        public long maxResponseTime;
        public long medianResponseTime;
        public long p90ResponseTime;
        public long p95ResponseTime;
        public long p99ResponseTime;
    }

    public static class ErrorSummary {
        public String errorMessage;
        public int count;

        public ErrorSummary(String errorMessage, int count) {
            this.errorMessage = errorMessage;
            this.count = count;
        }
    }
}