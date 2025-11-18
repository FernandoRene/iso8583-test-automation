# language: es
@E2E @Authorization @Critical
Característica: Autorización (Authorization Request - MTI 0100)
  Como usuario del sistema financiero
  Quiero realizar autorizaciones de transacciones a través del simulador ISO8583
  Para validar transacciones del exterior y ATM externos

  Antecedentes:
    Dado que el simulador ISO8583 está disponible en "http://localhost:8081"
    Y el servicio está en modo "REAL" conectado al autorizador
    Y la conexión con el autorizador está establecida

  @HappyPath @Smoke
  Escenario: Autorización de compra exitosa
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4218281008687192"
    Y el Track2 es "4218281008687192D2709101123456789"
    Y la terminal "POS999US" está configurada
    Y el comercio "999888777666555" está activo
    Y el monto es "45000"
    Y con processing code "000000"
    Y con país adquirente "840"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y el campo "responseMessage" debe contener "Aprobada"
    Y el tiempo de respuesta debe ser menor a 5000 milisegundos
    Y el mensaje ISO8583 debe tener MTI "0110"
    Y el campo 39 del mensaje ISO debe ser "00"

  @HappyPath
  Escenario: Autorización de retiro ATM externo
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "ATM777BR" está configurada
    Y el comercio "555444333222111" está activo
    Y el monto es "100000"
    Y con processing code "010000"
    Y con país adquirente "076"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el STAN debe ser único y secuencial

  @HappyPath
  Escenario: Autorización con conversión de moneda
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS555AR" está configurada
    Y el comercio "888777666555444" está activo
    Y el monto es "50000"
    Y con processing code "000000"
    Y con país adquirente "032"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"

  @ErrorHandling
  Escenario: Autorización excede límite de tarjeta
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001US" está configurada
    Y el comercio "999888777666555" está activo
    Y el monto es "999999999999"
    Y con processing code "000000"
    Y con país adquirente "840"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["51", "61", "96"]
    Y el mensaje de respuesta debe indicar rechazo

  @ErrorHandling
  Escenario: Autorización con tarjeta bloqueada
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4111111111111111"
    Y el Track2 es "4111111111111111D2709101000000000"
    Y la terminal "POS001US" está configurada
    Y el comercio "999888777666555" está activo
    Y el monto es "10000"
    Y con processing code "000000"
    Y con país adquirente "840"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["14", "25", "43", "51"]

  @ErrorHandling
  Escenario: Autorización con datos inválidos
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "1234567890123456"
    Y el Track2 es "INVALID_TRACK2"
    Y la terminal "POS001US" está configurada
    Y el comercio "999888777666555" está activo
    Y el monto es ""
    Y con processing code "000000"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y debo recibir errores de validación
    Y los errores deben incluir "PAN"

  @Performance
  Escenario: Múltiples autorizaciones consecutivas
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001US" está configurada
    Y el comercio "999888777666555" está activo
    Y el monto es "2500"
    Y con processing code "000000"
    Y con país adquirente "840"
    Cuando envío 5 solicitudes consecutivas
    Entonces todas las transacciones deben completarse exitosamente
    Y cada transacción debe tener un STAN único
    Y el tiempo promedio de respuesta debe ser menor a 3000 milisegundos

  @ISO8583Protocol
  Escenario: Verificar estructura del mensaje ISO8583 para Authorization
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001US" está configurada
    Y el comercio "999888777666555" está activo
    Y el monto es "35000"
    Y con processing code "000000"
    Y con país adquirente "840"
    Cuando envío la transacción
    Entonces el mensaje ISO8583 debe cumplir con:
      | Campo | Valor Esperado        | Tipo       |
      | MTI   | 0100                  | Exacto     |
      | 2     | 4532015112830366      | Exacto     |
      | 3     | 000000                | Exacto     |
      | 4     | 000000035000          | Exacto     |
      | 18    | 5999                  | Exacto     |
      | 19    | 840                   | Exacto     |
      | 22    | 010                   | Exacto     |
      | 25    | 08                    | Exacto     |
      | 41    | POS001US              | Exacto     |
      | 49    | 068                   | Exacto     |
    Y la respuesta debe tener MTI "0110"
    Y el campo 39 de la respuesta debe existir

  @ConnectionRecovery
  Escenario: Autorización después de reconexión
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001US" está configurada
    Y el comercio "999888777666555" está activo
    Y el monto es "18000"
    Y con processing code "000000"
    Y con país adquirente "840"
    Y se pierde la conexión con el autorizador
    Cuando se restablece la conexión automáticamente
    Y envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @Timeout
  Escenario: Autorización con timeout del autorizador
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001US" está configurada
    Y el comercio "999888777666555" está activo
    Y el monto es "12000"
    Y con processing code "000000"
    Y con país adquirente "840"
    Y el autorizador está configurado para no responder
    Cuando envío la transacción
    Entonces debería recibir un error de timeout
    Y el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el campo "errorType" debe ser "TIMEOUT"

  @FraudPrevention
  Escenario: Autorización desde país sospechoso
    Dado que preparo una transacción de tipo "AUTHORIZATION"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS999XX" está configurada
    Y el comercio "111222333444555" está activo
    Y el monto es "150000"
    Y con processing code "000000"
    Y con país adquirente "999"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
