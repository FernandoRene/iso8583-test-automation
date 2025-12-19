# language: es
@E2E @Cashback @Critical
Característica: Cashback
  Como usuario del sistema financiero
  Quiero realizar transacciones con cashback a través del simulador ISO8583
  Para retirar efectivo adicional junto con mi compra

  Antecedentes:
    Dado que el simulador ISO8583 está disponible en "http://localhost:8081"
    Y el servicio está en modo "MOCK" conectado al autorizador
    Y la conexión con el autorizador está establecida

  @HappyPath @Smoke
  Escenario: Cashback exitoso con MTI 0100 (Authorization)
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4218281015067172"
    Y el Track2 es "4218281015067172D29072261831543400000"
    Y la terminal "03209001" está configurada
    Y el comercio "000000404260" está activo
    Y el monto es "10600"
    Y el monto de cashback es "1576"
    Y el MTI es "0100"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y el campo "responseMessage" debe contener "Aprobada"
    Y el tiempo de respuesta debe ser menor a 5000 milisegundos
    Y el mensaje ISO8583 debe tener MTI "0110"
    Y el campo 39 del mensaje ISO debe ser "00"

  @HappyPath
  Escenario: Cashback exitoso con MTI 0200 (Financial)
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4218281015067172"
    Y el Track2 es "4218281015067172D29072261831543400000"
    Y la terminal "03209001" está configurada
    Y el comercio "000000404260" está activo
    Y el monto es "10600"
    Y el monto de cashback es "1576"
    Y el MTI es "0200"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"
    Y el mensaje ISO8583 debe tener MTI "0210"

  @HappyPath
  Escenario: Cashback con monto pequeño
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911" está activo
    Y el monto es "5000"
    Y el monto de cashback es "500"
    Y el MTI es "0100"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el STAN debe ser único y secuencial

  @HappyPath
  Escenario: Cashback con monto alto
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "50000"
    Y el monto de cashback es "5000"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @ErrorHandling
  Escenario: Cashback sin Track2 (campo obligatorio)
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Y el monto de cashback es "1000"
    Cuando envío la transacción sin Track2
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y debo recibir errores de validación
    Y los errores deben incluir "Track2"

  @ErrorHandling
  Escenario: Cashback sin monto de cashback (campo 54 requerido)
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Cuando envío la transacción sin cashback amount
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y debo recibir errores de validación
    Y los errores deben incluir "cashback"

  @ErrorHandling
  Escenario: Cashback excede límite disponible
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "999999999999"
    Y el monto de cashback es "99999999"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["51", "61", "96"]

  @ErrorHandling
  Escenario: Cashback con tarjeta inexistente
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4111111111111111"
    Y el Track2 es "4111111111111111D2709101000000000"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "10000"
    Y el monto de cashback es "1000"
    Cuando envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el código de respuesta debe ser uno de: ["14", "25", "51"]

  @Performance
  Escenario: Múltiples cashbacks consecutivos
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "15000"
    Y el monto de cashback es "1500"
    Cuando envío 5 solicitudes consecutivas
    Entonces todas las transacciones deben completarse exitosamente
    Y cada transacción debe tener un STAN único
    Y el tiempo promedio de respuesta debe ser menor a 3000 milisegundos

  @ISO8583Protocol
  Escenario: Verificar estructura del mensaje ISO8583 para Cashback
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4218281015067172"
    Y el Track2 es "4218281015067172D29072261831543400000"
    Y la terminal "03209001" está configurada
    Y el comercio "000000404260" está activo
    Y el monto es "10600"
    Y el monto de cashback es "1576"
    Y el MTI es "0100"
    Cuando envío la transacción
    Entonces el mensaje ISO8583 debe cumplir con:
      | Campo | Valor Esperado               | Tipo       |
      | MTI   | 0100                         | Exacto     |
      | 2     | 4218281015067172             | Exacto     |
      | 3     | 090000                       | Exacto     |
      | 4     | 000000010600                 | Exacto     |
      | 18    | 5411                         | Exacto     |
      | 22    | 051                          | Exacto     |
      | 25    | 00                           | Exacto     |
      | 32    | 416686                       | Exacto     |
      | 35    | 4218281015067172D29072261831543400000 | Exacto |
      | 41    | 03209001                     | Exacto     |
      | 42    | 000000404260                 | Contiene   |
      | 49    | 068                          | Exacto     |
      | 54    | 0040068D                     | Contiene   |
    Y la respuesta debe tener MTI "0110"
    Y el campo 39 de la respuesta debe existir
    Y el campo 54 de la respuesta debe existir

  @ConnectionRecovery
  Escenario: Cashback después de reconexión
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "12000"
    Y el monto de cashback es "1200"
    Y se pierde la conexión con el autorizador
    Cuando se restablece la conexión automáticamente
    Y envío la transacción
    Entonces el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "true"
    Y el campo "responseCode" debe ser "00"

  @Timeout
  Escenario: Cashback con timeout del autorizador
    Dado que preparo una transacción de tipo "CASHBACK"
    Y que tengo una tarjeta con PAN "4532015112830366"
    Y el Track2 es "4532015112830366D2709101123456789"
    Y la terminal "POS001LP" está configurada
    Y el comercio "409911000001234" está activo
    Y el monto es "8000"
    Y el monto de cashback es "800"
    Y el autorizador está configurado para no responder
    Cuando envío la transacción
    Entonces debería recibir un error de timeout
    Y el código de respuesta HTTP debe ser 200
    Y el campo "successful" debe ser "false"
    Y el campo "errorType" debe ser "TIMEOUT"