package com.iso8583.test.models;

/**
 * Enum para tipos de transacciones ISO8583 soportadas
 * Mapea directamente con los tipos del simulador
 */
public enum TransactionType {
    BALANCE_INQUIRY("BALANCE_INQUIRY", "Consulta de Saldo"),
    CASH_ADVANCE("CASH_ADVANCE", "Avance de Efectivo"),
    PURCHASE("PURCHASE", "Compra"),
    TRANSFER("TRANSFER", "Transferencia"),
    AUTHORIZATION("AUTHORIZATION", "Autorización");

    private final String code;
    private final String description;

    TransactionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Obtener tipo desde código string
     */
    public static TransactionType fromCode(String code) {
        for (TransactionType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de transacción desconocido: " + code);
    }

    @Override
    public String toString() {
        return code;
    }
}