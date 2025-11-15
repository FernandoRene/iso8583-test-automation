package com.iso8583.test.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo para responses de transacciones ISO8583
 * VERSIÓN SIN LOMBOK
 *
 * ✅ VERSIÓN CORREGIDA con métodos faltantes agregados
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse {

    @JsonProperty("successful")
    private Boolean successful;

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("responseMessage")
    private String responseMessage;

    @JsonProperty("stan")
    private String stan;

    @JsonProperty("mti")
    private String mti;

    @JsonProperty("fields")
    private Map<String, String> fields = new HashMap<>();

    private Map<Integer, String> isoFields = new HashMap<>();

    @JsonProperty("validationErrors")
    private List<String> validationErrors;

    // ✅ NUEVO: Para almacenar warnings (no críticos)
    @JsonProperty("validationWarnings")
    private List<String> validationWarnings;

    @JsonProperty("errorType")
    private String errorType;

    @JsonProperty("responseTime")
    private Long responseTime;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("balance")
    private String balance;

    @JsonProperty("approvalCode")
    private String approvalCode;

    @JsonProperty("retrievalReferenceNumber")
    private String retrievalReferenceNumber;

    @JsonProperty("privateData")
    private String privateData;

    // ✅ NUEVO: HTTP status code del response REST
    private Integer httpStatusCode;

    // Constructores
    public TransactionResponse() {
    }

    // ============================================================================
    // GETTERS Y SETTERS
    // ============================================================================

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields != null ? fields : new HashMap<>();
        syncFieldsToIsoFields();
    }

    public void setIsoFields(Map<Integer, String> isoFields) {
        this.isoFields = isoFields != null ? isoFields : new HashMap<>();
        syncIsoFieldsToFields();
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    // ✅ NUEVO: Getters y setters para validationWarnings
    public List<String> getValidationWarnings() {
        return validationWarnings != null ? validationWarnings : new ArrayList<>();
    }

    public void setValidationWarnings(List<String> validationWarnings) {
        this.validationWarnings = validationWarnings;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public String getRetrievalReferenceNumber() {
        return retrievalReferenceNumber;
    }

    public void setRetrievalReferenceNumber(String retrievalReferenceNumber) {
        this.retrievalReferenceNumber = retrievalReferenceNumber;
    }

    public String getPrivateData() {
        return privateData;
    }

    public void setPrivateData(String privateData) {
        this.privateData = privateData;
    }

    // ✅ NUEVO: Getters y setters para httpStatusCode
    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    // ============================================================================
    // MÉTODOS DE SINCRONIZACIÓN
    // ============================================================================

    private void syncFieldsToIsoFields() {
        if (fields == null) return;

        isoFields.clear();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            try {
                int fieldNumber = Integer.parseInt(entry.getKey());
                isoFields.put(fieldNumber, entry.getValue());
            } catch (NumberFormatException e) {
                // Ignorar keys no numéricos
            }
        }
    }

    private void syncIsoFieldsToFields() {
        if (isoFields == null) return;

        fields.clear();
        for (Map.Entry<Integer, String> entry : isoFields.entrySet()) {
            fields.put(String.valueOf(entry.getKey()), entry.getValue());
        }
    }

    // ============================================================================
    // MÉTODOS PARA ACCEDER A CAMPOS ISO8583
    // ============================================================================

    public String getIsoField(int fieldNumber) {
        if (isoFields.isEmpty() && !fields.isEmpty()) {
            syncFieldsToIsoFields();
        }
        return isoFields.get(fieldNumber);
    }

    public String getField(String fieldNumber) {
        return fields.get(fieldNumber);
    }

    public void setIsoField(int fieldNumber, String value) {
        isoFields.put(fieldNumber, value);
        fields.put(String.valueOf(fieldNumber), value);
    }

    public boolean hasIsoField(int fieldNumber) {
        if (isoFields.isEmpty() && !fields.isEmpty()) {
            syncFieldsToIsoFields();
        }
        return isoFields.containsKey(fieldNumber) && isoFields.get(fieldNumber) != null;
    }

    public Integer getFieldAsInt(String fieldNumber) {
        String value = getField(fieldNumber);
        return value != null ? Integer.parseInt(value) : null;
    }

    public Map<Integer, String> getIsoFields() {
        if (isoFields.isEmpty() && !fields.isEmpty()) {
            syncFieldsToIsoFields();
        }
        return new HashMap<>(isoFields);
    }

    // ============================================================================
    // MÉTODOS UTILITARIOS
    // ============================================================================

    public boolean isApproved() {
        return "00".equals(responseCode);
    }

    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    // ✅ NUEVO: Método para verificar si hay warnings
    public boolean hasValidationWarnings() {
        return validationWarnings != null && !validationWarnings.isEmpty();
    }

    public boolean isTimeout() {
        return "TIMEOUT".equalsIgnoreCase(errorType);
    }

    public boolean hasMTI(String expectedMTI) {
        return expectedMTI != null && expectedMTI.equals(mti);
    }

    public String getErrorMessage() {
        if (hasValidationErrors()) {
            return String.join(", ", validationErrors);
        }
        if (errorType != null) {
            return errorType + ": " + responseMessage;
        }
        return responseMessage;
    }

    // ✅ NUEVO: Método para obtener RRN (alias de retrievalReferenceNumber)
    public String getRrn() {
        return retrievalReferenceNumber;
    }

    public void setRrn(String rrn) {
        this.retrievalReferenceNumber = rrn;
    }

    // ✅ NUEVO: Método de conveniencia para verificar éxito HTTP
    public boolean isHttpSuccess() {
        return httpStatusCode != null && httpStatusCode >= 200 && httpStatusCode < 300;
    }

    // ============================================================================
    // MÉTODOS PARA DEBUGGING
    // ============================================================================

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "successful=" + successful +
                ", responseCode='" + responseCode + '\'' +
                ", responseMessage='" + responseMessage + '\'' +
                ", stan='" + stan + '\'' +
                ", mti='" + mti + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                ", responseTime=" + responseTime +
                ", approvalCode='" + approvalCode + '\'' +
                ", hasErrors=" + hasValidationErrors() +
                ", hasWarnings=" + hasValidationWarnings() +
                '}';
    }
}