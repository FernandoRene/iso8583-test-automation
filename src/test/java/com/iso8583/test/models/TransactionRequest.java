package com.iso8583.test.models;

/**
 * Modelo completo para requests de transacciones ISO8583
 * VERSIÓN SIN LOMBOK - Con getters/setters manuales y Builder manual
 */
public class TransactionRequest {

    // Campos comunes a todas las transacciones
    private TransactionType transactionType;
    private String pan;
    private String track2;
    private String amount;
    private String terminalId;
    private String cardAcceptorId;
    private String cardAcceptorName;
    private String currencyCode;
    private String processingCode;

    // Campos específicos de algunas transacciones
    private String account;
    private String targetAccount;
    private String billingAmount;
    private String billingCurrency;
    private String acquiringCountry;
    private String acquiringInstitution;
    private String merchantType;
    private String posEntryMode;
    private String pinData;
    private String privateUseFields;
    private String cashbackAmount;  // Campo 54 - Monto de cashback
    private String mti;              // MTI específico (0100 o 0200 para cashback)

    // Constructores
    public TransactionRequest() {
    }

    private TransactionRequest(Builder builder) {
        this.transactionType = builder.transactionType;
        this.pan = builder.pan;
        this.track2 = builder.track2;
        this.amount = builder.amount;
        this.terminalId = builder.terminalId;
        this.cardAcceptorId = builder.cardAcceptorId;
        this.cardAcceptorName = builder.cardAcceptorName;
        this.currencyCode = builder.currencyCode;
        this.processingCode = builder.processingCode;
        this.account = builder.account;
        this.targetAccount = builder.targetAccount;
        this.billingAmount = builder.billingAmount;
        this.billingCurrency = builder.billingCurrency;
        this.acquiringCountry = builder.acquiringCountry;
        this.acquiringInstitution = builder.acquiringInstitution;
        this.merchantType = builder.merchantType;
        this.posEntryMode = builder.posEntryMode;
        this.pinData = builder.pinData;
        this.privateUseFields = builder.privateUseFields;
        this.cashbackAmount = builder.cashbackAmount;
        this.mti = builder.mti;
    }

    // ============================================================================
    // GETTERS Y SETTERS
    // ============================================================================

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getCardAcceptorId() {
        return cardAcceptorId;
    }

    public void setCardAcceptorId(String cardAcceptorId) {
        this.cardAcceptorId = cardAcceptorId;
    }

    public String getCardAcceptorName() {
        return cardAcceptorName;
    }

    public void setCardAcceptorName(String cardAcceptorName) {
        this.cardAcceptorName = cardAcceptorName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(String targetAccount) {
        this.targetAccount = targetAccount;
    }

    public String getBillingAmount() {
        return billingAmount;
    }

    public void setBillingAmount(String billingAmount) {
        this.billingAmount = billingAmount;
    }

    public String getBillingCurrency() {
        return billingCurrency;
    }

    public void setBillingCurrency(String billingCurrency) {
        this.billingCurrency = billingCurrency;
    }

    public String getAcquiringCountry() {
        return acquiringCountry;
    }

    public void setAcquiringCountry(String acquiringCountry) {
        this.acquiringCountry = acquiringCountry;
    }

    public String getAcquiringInstitution() {
        return acquiringInstitution;
    }

    public void setAcquiringInstitution(String acquiringInstitution) {
        this.acquiringInstitution = acquiringInstitution;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getPosEntryMode() {
        return posEntryMode;
    }

    public void setPosEntryMode(String posEntryMode) {
        this.posEntryMode = posEntryMode;
    }

    public String getPinData() {
        return pinData;
    }

    public void setPinData(String pinData) {
        this.pinData = pinData;
    }

    public String getPrivateUseFields() {
        return privateUseFields;
    }

    public void setPrivateUseFields(String privateUseFields) {
        this.privateUseFields = privateUseFields;
    }

    public String getCashbackAmount() {
        return cashbackAmount;
    }

    public void setCashbackAmount(String cashbackAmount) {
        this.cashbackAmount = cashbackAmount;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    // ============================================================================
    // MÉTODOS AUXILIARES
    // ============================================================================

    /**
     * Obtiene PAN enmascarado para logging seguro
     */
    public String getMaskedPan() {
        if (pan == null || pan.length() < 10) {
            return "****";
        }
        String first4 = pan.substring(0, 4);
        String last4 = pan.substring(pan.length() - 4);
        return first4 + "********" + last4;
    }

    /**
     * Obtiene Track2 enmascarado para logging seguro
     */
    public String getMaskedTrack2() {
        if (track2 == null || track2.length() < 10) {
            return "****";
        }
        String first4 = track2.substring(0, 4);
        return first4 + "********";
    }

    /**
     * Verifica si tiene los campos requeridos básicos
     */
    public boolean hasRequiredFields() {
        return pan != null && !pan.isEmpty()
                && track2 != null && !track2.isEmpty()
                && terminalId != null && !terminalId.isEmpty()
                && cardAcceptorId != null && !cardAcceptorId.isEmpty();
    }

    /**
     * Verifica si es una transacción válida
     */
    public boolean isValid() {
        return transactionType != null && hasRequiredFields();
    }

    /**
     * Obtiene descripción corta para logging
     */
    public String getDescription() {
        return String.format("%s - PAN: %s, Amount: %s",
                transactionType != null ? transactionType.name() : "UNKNOWN",
                getMaskedPan(),
                amount != null ? amount : "N/A"
        );
    }

    // ============================================================================
    // BUILDER PATTERN MANUAL
    // ============================================================================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TransactionType transactionType;
        private String pan;
        private String track2;
        private String amount;
        private String terminalId;
        private String cardAcceptorId;
        private String cardAcceptorName;
        private String currencyCode;
        private String processingCode;
        private String account;
        private String targetAccount;
        private String billingAmount;
        private String billingCurrency;
        private String acquiringCountry;
        private String acquiringInstitution;
        private String merchantType;
        private String posEntryMode;
        private String pinData;
        private String privateUseFields;
        private String cashbackAmount;
        private String mti;

        private Builder() {
        }

        public Builder transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder pan(String pan) {
            this.pan = pan;
            return this;
        }

        public Builder track2(String track2) {
            this.track2 = track2;
            return this;
        }

        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder terminalId(String terminalId) {
            this.terminalId = terminalId;
            return this;
        }

        public Builder cardAcceptorId(String cardAcceptorId) {
            this.cardAcceptorId = cardAcceptorId;
            return this;
        }

        public Builder cardAcceptorName(String cardAcceptorName) {
            this.cardAcceptorName = cardAcceptorName;
            return this;
        }

        public Builder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public Builder processingCode(String processingCode) {
            this.processingCode = processingCode;
            return this;
        }

        public Builder account(String account) {
            this.account = account;
            return this;
        }

        public Builder targetAccount(String targetAccount) {
            this.targetAccount = targetAccount;
            return this;
        }

        public Builder billingAmount(String billingAmount) {
            this.billingAmount = billingAmount;
            return this;
        }

        public Builder billingCurrency(String billingCurrency) {
            this.billingCurrency = billingCurrency;
            return this;
        }

        public Builder acquiringCountry(String acquiringCountry) {
            this.acquiringCountry = acquiringCountry;
            return this;
        }

        public Builder acquiringInstitution(String acquiringInstitution) {
            this.acquiringInstitution = acquiringInstitution;
            return this;
        }

        public Builder merchantType(String merchantType) {
            this.merchantType = merchantType;
            return this;
        }

        public Builder posEntryMode(String posEntryMode) {
            this.posEntryMode = posEntryMode;
            return this;
        }

        public Builder pinData(String pinData) {
            this.pinData = pinData;
            return this;
        }

        public Builder privateUseFields(String privateUseFields) {
            this.privateUseFields = privateUseFields;
            return this;
        }

        // NUEVOS MÉTODOS
        public Builder cashbackAmount(String cashbackAmount) {
            this.cashbackAmount = cashbackAmount;
            return this;
        }

        public Builder mti(String mti) {
            this.mti = mti;
            return this;
        }

        /**
         * Aplica valores por defecto comunes
         */
        public Builder applyDefaults() {
            if (this.terminalId == null) {
                this.terminalId = "TERM0001";
            }
            if (this.cardAcceptorId == null) {
                this.cardAcceptorId = "123456789012345";
            }
            if (this.cardAcceptorName == null) {
                this.cardAcceptorName = "TEST MERCHANT LOCATION";
            }
            if (this.currencyCode == null) {
                this.currencyCode = "068";
            }
            return this;
        }

        /**
         * Construye el TransactionRequest
         */
        public TransactionRequest build() {
            return new TransactionRequest(this);
        }
    }
}