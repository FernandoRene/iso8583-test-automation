# language: es
@E2E @Transfer @Critical
Característica: Transferencia (Transfer)
  Como usuario del sistema financiero
  Quiero realizar transferencias a través del simulador ISO8583
  Para mover fondos entre cuentas

  Antecedentes:
    Dado que el simulador ISO8583 está disponible en "http://localhost:8081"
    Y el servicio está en modo "REAL" conectado al autorizador
    Y la conexión con el autorizador está establecida

  @HappyPath @Smoke
  Escenario: Transferencia entre cuentas propias exitosa
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4218281008687192"
    Y el Track2 es "4218281008687192D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "100000"
    Y con cuenta origen "1310672398"
    Y con cuenta destino "1310672399"
    Y con processing code "400040"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y el campo "responseMessage" debe contener "Aprobada"
    Y el tiempo de respuesta debe ser menor a 5000 milisegundos
    Y el mensaje ISO8583 debe tener MTI "0210"
    Y el campo 39 del mensaje ISO debe ser "00"

  @HappyPath
  Escenario: Transferencia a cuenta de terceros
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM002LP" está configurada
    Y el comercio "409911" está activo
    Y el monto es "50000"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "10019876543"
    Y con processing code "400020"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el STAN debe ser único y secuencial

  @HappyPath
  Escenario: Transferencia a cuenta de otro banco
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "75000"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "20099887766"
    Y con processing code "400060"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"

  @HappyPath
  Escenario: Transferencia interbancaria
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "120000"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "30055443322"
    Y con processing code "400080"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"

  @ErrorHandling
  Escenario: Transferencia excede saldo disponible
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "999999999999"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "10019876543"
    Y con processing code "400040"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["51", "61", "96"]
    Y el mensaje de respuesta debe indicar rechazo

  @ErrorHandling
  Escenario: Transferencia con cuenta destino inválida
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "99999999999"
    Y con processing code "400040"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["14", "25", "51"]

  @ErrorHandling
  Escenario: Transferencia con datos inválidos
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "1234567890123456"
    Y el Track2 es "INVALID_TRACK2"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es ""
    Y con cuenta origen ""
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y debo recibir errores de validación
    Y los errores deben incluir "PAN"

  @Performance
  Escenario: Múltiples transferencias consecutivas
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "5000"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "10019876543"
    Y con processing code "400040"
    Cuando envío 5 solicitudes consecutivas
    Entonces todas las transacciones deben completarse exitosamente
    Y cada transacción debe tener un STAN único
    Y el tiempo promedio de respuesta debe ser menor a 3000 milisegundos

  @ISO8583Protocol
  Escenario: Verificar estructura del mensaje ISO8583 para Transfer
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "30000"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "10019876543"
    Y con processing code "400040"
    Cuando envío la transacción
    Entonces el mensaje ISO8583 debe cumplir con:
      | Campo | Valor Esperado        | Tipo       |
      | MTI   | 0200                  | Exacto     |
      | 2     | 4532015112830366      | Exacto     |
      | 3     | 400040                | Exacto     |
      | 4     | 000000030000          | Exacto     |
      | 18    | 6011                  | Exacto     |
      | 22    | 021                   | Exacto     |
      | 25    | 02                    | Exacto     |
      | 32    | 409911                | Exacto     |
      | 41    | ATM001LP              | Exacto     |
      | 49    | 068                   | Exacto     |
      | 102   | 10012345678           | Exacto     |
      | 103   | 10019876543           | Exacto     |
    Y la respuesta debe tener MTI "0210"
    Y el campo 39 de la respuesta debe existir

  @ConnectionRecovery
  Escenario: Transferencia después de reconexión
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "25000"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "10019876543"
    Y con processing code "400040"
    Y se pierde la conexión con el autorizador
    Cuando se restablece la conexión automáticamente
    Y envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @Timeout
  Escenario: Transferencia con timeout del autorizador
    Dado que preparo una transacción de tipo "TRANSFER"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "15000"
    Y con cuenta origen "10012345678"
    Y con cuenta destino "10019876543"
    Y con processing code "400040"
    Y el autorizador está configurado para no responder
    Cuando envío la transacción
    Entonces debería recibir un error de timeout
    Y el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el campo "errorType" debe ser "TIMEOUT"
