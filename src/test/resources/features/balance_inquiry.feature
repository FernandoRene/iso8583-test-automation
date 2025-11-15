# language: es
@E2E @BalanceInquiry @Critical
Característica: Consulta de Saldo (Balance Inquiry)
  Como usuario del sistema financiero
  Quiero realizar consultas de saldo a través del simulador ISO8583
  Para verificar el saldo disponible en mis cuentas

  Antecedentes:
    Dado que el simulador ISO8583 está disponible en "http://localhost:8081"
    Y el servicio está en modo "REAL" conectado al autorizador
    Y la conexión con el autorizador está establecida

  @HappyPath @Smoke
  Escenario: Consulta de saldo exitosa para tarjeta válida
    Dado que tengo una tarjeta con PAN "4218281008687192"
    Y el Track2 es "4218281008687192D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y la cuenta a consultar es "1310672398"
    Cuando envío una solicitud de consulta de saldo
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y el campo "responseMessage" debe contener "Aprobada"
    Y el tiempo de respuesta debe ser menor a 5000 milisegundos
    Y el mensaje ISO8583 debe tener MTI "0210"
    Y el campo 39 del mensaje ISO debe ser "00"

  @HappyPath
  Escenario: Consulta de saldo con cuenta específica
    Dado que tengo una tarjeta con PAN "4218281008687192"
    Y el Track2 es "4218281008687192D27091011234567"
    Y la terminal "ATM002LP" está configurada
    Y el comercio "409911" está activo
    Y la cuenta a consultar es "1310672398"
    Cuando envío una solicitud de consulta de saldo
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y debo recibir información de saldo
    Y el STAN debe ser único y secuencial

  @ErrorHandling
  Escenario: Consulta de saldo para tarjeta inexistente
    Dado que tengo una tarjeta con PAN "4111111111111111"
    Y el Track2 es "4111111111111111D2709101000000000"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y la cuenta a consultar es "99999999999"
    Cuando envío una solicitud de consulta de saldo
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["14", "25", "51"]
    Y el mensaje de respuesta debe indicar rechazo

  @ErrorHandling
  Escenario: Consulta de saldo con datos inválidos
    Dado que tengo una tarjeta con PAN "1234567890123456"
    Y el Track2 es "INVALID_TRACK2"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y la cuenta a consultar es ""
    Cuando envío una solicitud de consulta de saldo
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y debo recibir errores de validación
    Y los errores deben incluir "PAN"

  @Performance
  Escenario: Múltiples consultas de saldo consecutivas
    Dado que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y la cuenta a consultar es "10012345678"
    Cuando envío 5 solicitudes de consulta de saldo consecutivas
    Entonces todas las transacciones deben completarse exitosamente
    Y cada transacción debe tener un STAN único
    Y el tiempo promedio de respuesta debe ser menor a 3000 milisegundos

  @ConnectionRecovery
  Escenario: Consulta de saldo después de reconexión
    Dado que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y la cuenta a consultar es "10012345678"
    Y se pierde la conexión con el autorizador
    Cuando se restablece la conexión automáticamente
    Y envío una solicitud de consulta de saldo
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @ISO8583Protocol
  Escenario: Verificar estructura del mensaje ISO8583 para Balance Inquiry
    Dado que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y la cuenta a consultar es "10012345678"
    Cuando envío una solicitud de consulta de saldo
    Entonces el mensaje ISO8583 debe cumplir con:
      | Campo | Valor Esperado        | Tipo       |
      | MTI   | 0200                  | Exacto     |
      | 2     | 4532015112830366      | Exacto     |
      | 3     | 301099                | Exacto     |
      | 4     | 000000000000          | Exacto     |
      | 18    | 6011                  | Exacto     |
      | 22    | 051                   | Exacto     |
      | 25    | 00                    | Exacto     |
      | 32    | 409911                | Exacto     |
      | 41    | ATM001LP              | Exacto     |
      | 49    | 068                   | Exacto     |
      | 102   | 10012345678           | Exacto     |
    Y la respuesta debe tener MTI "0210"
    Y el campo 39 de la respuesta debe existir

  @Timeout
  Escenario: Consulta de saldo con timeout del autorizador
    Dado que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y la cuenta a consultar es "10012345678"
    Y el autorizador está configurado para no responder
    Cuando envío una solicitud de consulta de saldo
    Entonces debería recibir un error de timeout
    Y el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el campo "errorType" debe ser "TIMEOUT"