# language: es
@E2E @Purchase @Critical
Característica: Compra (Purchase)
  Como usuario del sistema financiero
  Quiero realizar compras a través del simulador ISO8583
  Para pagar bienes y servicios con mi tarjeta

  Antecedentes:
    Dado que el simulador ISO8583 está disponible en "http://localhost:8081"
    Y el servicio está en modo "MOCK" conectado al autorizador
    Y la conexión con el autorizador está establecida

  @HappyPath @Smoke
  Escenario: Compra exitosa para tarjeta válida
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4218281008687192"
    Y el Track2 es "4218281008687192D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "35000"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y el campo "responseMessage" debe contener "Aprobada"
    Y el tiempo de respuesta debe ser menor a 5000 milisegundos
    Y el mensaje ISO8583 debe tener MTI "0210"
    Y el campo 39 del mensaje ISO debe ser "00"

  @HappyPath
  Escenario: Compra con monto pequeño
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS002LP" está configurada
    Y el comercio "409911" está activo
    Y el monto es "100"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el STAN debe ser único y secuencial

  @HappyPath
  Escenario: Compra con monto alto
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "500000"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @ErrorHandling
  Escenario: Compra excede límite disponible
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "999999999999"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["51", "61", "96"]
    Y el mensaje de respuesta debe indicar rechazo

  @ErrorHandling
  Escenario: Compra con tarjeta inexistente
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4111111111111111"
    Y el Track2 es "4111111111111111D2709101000000000"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["14", "25", "51"]

  @ErrorHandling
  Escenario: Compra con datos inválidos
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "1234567890123456"
    Y el Track2 es "INVALID_TRACK2"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es ""
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y debo recibir errores de validación
    Y los errores deben incluir "PAN"

  @Performance
  Escenario: Múltiples compras consecutivas
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "1500"
    Cuando envío 5 solicitudes consecutivas
    Entonces todas las transacciones deben completarse exitosamente
    Y cada transacción debe tener un STAN único
    Y el tiempo promedio de respuesta debe ser menor a 3000 milisegundos

  @ISO8583Protocol
  Escenario: Verificar estructura del mensaje ISO8583 para Purchase
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "25000"
    Cuando envío la transacción
    Entonces el mensaje ISO8583 debe cumplir con:
      | Campo | Valor Esperado        | Tipo       |
      | MTI   | 0200                  | Exacto     |
      | 2     | 4532015112830366      | Exacto     |
      | 3     | 000000                | Exacto     |
      | 4     | 000000025000          | Exacto     |
      | 18    | 5999                  | Exacto     |
      | 22    | 051                   | Exacto     |
      | 25    | 00                    | Exacto     |
      | 32    | 409911                | Exacto     |
      | 41    | POS001LP              | Exacto     |
      | 49    | 068                   | Exacto     |
    Y la respuesta debe tener MTI "0210"
    Y el campo 39 de la respuesta debe existir

  @ConnectionRecovery
  Escenario: Compra después de reconexión
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "12000"
    Y se pierde la conexión con el autorizador
    Cuando se restablece la conexión automáticamente
    Y envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @Timeout
  Escenario: Compra con timeout del autorizador
    Dado que preparo una transacción de tipo "PURCHASE"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "8000"
    Y el autorizador está configurado para no responder
    Cuando envío la transacción
    Entonces debería recibir un error de timeout
    Y el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el campo "errorType" debe ser "TIMEOUT"
