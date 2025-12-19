# language: es
@E2E @Deposit @Critical
Característica: Depósito (Deposit)
  Como usuario del sistema financiero
  Quiero realizar depósitos a través del simulador ISO8583
  Para depositar dinero en cuentas bancarias

  Antecedentes:
    Dado que el simulador ISO8583 está disponible en "http://localhost:8081"
    Y el servicio está en modo "MOCK" conectado al autorizador
    Y la conexión con el autorizador está establecida

  @HappyPath @Smoke
  Escenario: Depósito exitoso en cuenta propia
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4218281008687192"
    Y el Track2 es "4218281008687192D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "100000"
    Y la cuenta destino es "10001234567"
    Y el processing code es "210003"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y el campo "responseMessage" debe contener "Aprobada"
    Y el tiempo de respuesta debe ser menor a 5000 milisegundos
    Y el mensaje ISO8583 debe tener MTI "0210"
    Y el campo 39 del mensaje ISO debe ser "00"

  @HappyPath
  Escenario: Depósito en cuenta de tercero
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM002LP" está configurada
    Y el comercio "409911" está activo
    Y el monto es "50000"
    Y la cuenta destino es "20009876543"
    Y el processing code es "210004"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @HappyPath
  Escenario: Depósito sin Track2 (opcional)
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4218281008687192"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "25000"
    Y la cuenta destino es "10001234567"
    Y el processing code es "210003"
    Cuando envío la transacción sin Track2
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"

  @HappyPath
  Escenario: Depósito con monto alto
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "500000"
    Y la cuenta destino es "10001234567"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @ErrorHandling
  Escenario: Depósito sin cuenta destino (campo 103 requerido)
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Cuando envío la transacción sin cuenta destino
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y debo recibir errores de validación
    Y los errores deben incluir "cuenta destino"

  @ErrorHandling
  Escenario: Depósito con cuenta destino inválida
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Y la cuenta destino es "INVALID_ACCOUNT"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"

  @ErrorHandling
  Escenario: Depósito excede límite disponible
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "999999999999"
    Y la cuenta destino es "10001234567"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["51", "61", "96"]

  @Performance
  Escenario: Múltiples depósitos consecutivos
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "5000"
    Y la cuenta destino es "10001234567"
    Cuando envío 5 solicitudes consecutivas
    Entonces todas las transacciones deben completarse exitosamente
    Y cada transacción debe tener un STAN único
    Y el tiempo promedio de respuesta debe ser menor a 3000 milisegundos

  @ISO8583Protocol
  Escenario: Verificar estructura del mensaje ISO8583 para Deposit
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "75000"
    Y la cuenta destino es "10001234567"
    Y el processing code es "210003"
    Cuando envío la transacción
    Entonces el mensaje ISO8583 debe cumplir con:
      | Campo | Valor Esperado        | Tipo       |
      | MTI   | 0200                  | Exacto     |
      | 2     | 4532015112830366      | Exacto     |
      | 3     | 210003                | Exacto     |
      | 4     | 000000075000          | Exacto     |
      | 18    | 6011                  | Exacto     |
      | 22    | 051                   | Exacto     |
      | 25    | 00                    | Exacto     |
      | 32    | 409911                | Exacto     |
      | 41    | ATM001LP              | Exacto     |
      | 49    | 068                   | Exacto     |
      | 103   | 10001234567           | Exacto     |
    Y la respuesta debe tener MTI "0210"
    Y el campo 39 de la respuesta debe existir

  @ConnectionRecovery
  Escenario: Depósito después de reconexión
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "12000"
    Y la cuenta destino es "10001234567"
    Y se pierde la conexión con el autorizador
    Cuando se restablece la conexión automáticamente
    Y envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @Timeout
  Escenario: Depósito con timeout del autorizador
    Dado que preparo una transacción de tipo "DEPOSIT"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "8000"
    Y la cuenta destino es "10001234567"
    Y el autorizador está configurado para no responder
    Cuando envío la transacción
    Entonces debería recibir un error de timeout
    Y el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el campo "errorType" debe ser "TIMEOUT"