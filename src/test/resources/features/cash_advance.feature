# language: es
@E2E @CashAdvance @Critical
Característica: Avance de Efectivo (Cash Advance)
  Como usuario del sistema financiero
  Quiero realizar avances de efectivo a través del simulador ISO8583
  Para obtener efectivo de mi cuenta mediante tarjeta

  Antecedentes:
    Dado que el simulador ISO8583 está disponible en "http://localhost:8081"
    Y el servicio está en modo "REAL" conectado al autorizador
    Y la conexión con el autorizador está establecida

  @HappyPath @Smoke
  Escenario: Avance de efectivo exitoso para tarjeta válida
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "4218281008687192"
    Y el Track2 es "4218281008687192D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "50000"
    Y con cuenta "1310672398"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y el campo "responseMessage" debe contener "Aprobada"
    Y el tiempo de respuesta debe ser menor a 5000 milisegundos
    Y el mensaje ISO8583 debe tener MTI "0210"
    Y el campo 39 del mensaje ISO debe ser "00"

  @HappyPath
  Escenario: Avance de efectivo con monto alto
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM002LP" está configurada
    Y el comercio "409911" está activo
    Y el monto es "200000"
    Y con cuenta "10012345678"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el STAN debe ser único y secuencial

  @ErrorHandling
  Escenario: Avance de efectivo excede límite disponible
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "999999999999"
    Y con cuenta "10012345678"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["51", "61", "96"]
    Y el mensaje de respuesta debe indicar rechazo

  @ErrorHandling
  Escenario: Avance de efectivo con tarjeta inexistente
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "4111111111111111"
    Y el Track2 es "4111111111111111D2709101000000000"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Y con cuenta "99999999999"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["14", "25", "51"]

  @ErrorHandling
  Escenario: Avance de efectivo con datos inválidos
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "1234567890123456"
    Y el Track2 es "INVALID_TRACK2"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es ""
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y debo recibir errores de validación
    Y los errores deben incluir "PAN"

  @Performance
  Escenario: Múltiples avances de efectivo consecutivos
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "5000"
    Y con cuenta "10012345678"
    Cuando envío 5 solicitudes consecutivas
    Entonces todas las transacciones deben completarse exitosamente
    Y cada transacción debe tener un STAN único
    Y el tiempo promedio de respuesta debe ser menor a 3000 milisegundos

  @ISO8583Protocol
  Escenario: Verificar estructura del mensaje ISO8583 para Cash Advance
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Y con cuenta "10012345678"
    Cuando envío la transacción
    Entonces el mensaje ISO8583 debe cumplir con:
      | Campo | Valor Esperado        | Tipo       |
      | MTI   | 0210                  | Exacto     |
      | 2     | 4532015112830366      | Exacto     |
      | 3     | 011099                | Exacto     |
      | 4     | 000000010000          | Exacto     |
      | 18    | 6011                  | Exacto     |
      | 32    | 409911                | Exacto     |
      | 41    | ATM001LP              | Exacto     |
      | 49    | 068                   | Exacto     |
    Y la respuesta debe tener MTI "0210"
    Y el campo 39 de la respuesta debe existir

  @ConnectionRecovery
  Escenario: Avance de efectivo después de reconexión
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "15000"
    Y con cuenta "10012345678"
    Y se pierde la conexión con el autorizador
    Cuando se restablece la conexión automáticamente
    Y envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @Timeout
  Escenario: Avance de efectivo con timeout del autorizador
    Dado que preparo una transacción de tipo "CASH_ADVANCE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "20000"
    Y con cuenta "10012345678"
    Y el autorizador está configurado para no responder
    Cuando envío la transacción
    Entonces debería recibir un error de timeout
    Y el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el campo "errorType" debe ser "TIMEOUT"
