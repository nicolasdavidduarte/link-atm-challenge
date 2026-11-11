# Documento Técnico: Decisiones de Arquitectura y Diseño - Link ATM Challenge

Este documento detalla las decisiones de diseño, los patrones arquitectónicos implementados y las compensaciones asumidas durante el desarrollo del desafío técnico de producto ATM.

---

## 1. Introducción y Arquitectura General

El sistema está diseñado bajo una arquitectura cliente-servidor desacoplada, estructurada en un proyecto multimódulo de Maven que comprende:
* **`atm-web-service`**: Servicio backend que expone una API REST nativa mediante `com.sun.net.httpserver`, encapsulando la lógica de negocio, reglas de cajero, mecanismos de idempotencia y trazabilidad transaccional.
* **`atm-desktop`**: Aplicación de consola (CLI) desarrollada en Java que actúa como cliente HTTP utilizando `HttpURLConnection` para consumir las operaciones del servicio.

Ambos módulos cumplen estrictamente con la restricción de versión **Java 8 (JDK 1.8)** validada mediante el plugin de Maven Enforcer.

---

## 2. Decisiones de Diseño y Patrones de Ingeniería

### A. Garantía de Idempotencia en Operaciones Financieras
Para evitar duplicidad en transacciones críticas (como extracciones y depósitos) debido a reintentos de red o fallos del cliente, se implementó el componente `IdempotencyService`:
* **Header Estándar:** Las operaciones extraer y depositar exigen el envío del header HTTP `Idempotency-Key`, aunque al ser invocadas desde la consola dicha key se puede omitir y en este caso el cliente desktop creará una key aleatórea que será enviada al servicio web.
* **Validación de Huella Digital (*Request Hashing*):** Se calcula un hash SHA-256 del cuerpo de la petición (`requestHash`). Si una misma clave de idempotencia es reutilizada con un payload o una ruta diferente, el servicio rechaza inmediatamente la solicitud con un error `400 / IllegalStateException`, protegiendo el sistema contra colisiones o uso indebido de llaves.
* **Control de Concurrencia Transitoria:** Las solicitudes se registran inicialmente bajo un estado `PROCESSING`. Si dos hilos concurrentes intentan procesar la misma clave simultáneamente, el segundo hilo detecta el estado en curso y rechaza la ejecución paralela, devolviendo el resultado previamente almacenado una vez completado el primer hilo.

### B. Seguridad Concurrente (*Thread Safety*)
Dado que el servicio web maneja un entorno en memoria:
* Se utiliza `ConcurrentHashMap` y `CopyOnWriteArrayList` para el almacenamiento seguro de los datos entre múltiples hilos concurrentes.
* Se incorporaron bloques de sincronización explícitos (`synchronized`) a nivel de entidad en los métodos modificatorios de `Cuenta` (`extraer` y `depositar`) para prevenir condiciones de carrera ante solicitudes simultáneas sobre una misma cuenta bancaria.

### C. Auditoría y Trazabilidad Transaccional
Para cumplir con el requerimiento estricto de trazabilidad en transacciones monetarias:
* Cada operación (exitosa o fallida) genera un identificador único de transacción (`TX-<timestamp>`).
* La información se persiste de forma concurrente y segura en un archivo de auditoría local en formato .csv (comma separated values), registrando fecha, identificador, tipo de operación, tarjeta, cuenta/CBU, importe formateado y resultado booleano.
* Se eligió este estándar por su su fácil lectura y simplicidad operativa ya que permite que herramientas de análisis, hojas de cálculo o scripts externos consuman los registros sin conectores específicos.

---

## 3. Estrategia de Pruebas (Testing)

La suite de pruebas fue desarrollada utilizando **JUnit** y **Mockito** con las últimas versiones compatibles con Java 8.

---

## 4. Concesiones y Compensaciones

* **Persistencia en Memoria:** Se optó por repositorios basados en estructuras en memoria (`ConcurrentHashMap` / `Optional`) para ajustarse al enunciado del desafío y facilitar la ejecución inmediata sin dependencias externas de bases de datos. No obstante, el diseño mantiene una separación estricta mediante capas (*Controllers*, *Services*, *Repositories*) para respetar la arquitectura.

* **Almacenamiento de Auditoría en Archivo Local:** El registro transaccional se resolvió escribiendo en un archivo CSV local priorizando la simplicidad de despliegue y el cumplimiento directo de la consigna del challenge.


## 5. Stack Tecnológico y Justificación de Dependencias

Para resolver los requerimientos del desafío manteniendo la restricción estricta de **Java 8 (JDK 1.8)**, se incorporaron únicamente las librerías externas indispensables para la serialización, el testing y la gestión de compilación:

* **`com.google.code.gson:gson` (Versión 2.10.1):** 
  * Utilizada para la serialización y deserialización eficiente de objetos Java hacia JSON y viceversa. Es clave en el transporte de datos entre el cliente CLI (`atm-desktop`) y el servicio web (`atm-web-service`) a través de las peticiones y respuestas HTTP.
* **`com.squareup.okhttp3:okhttp` (Versión 4.x / compatible con Java 8):**
  * Utilizada por el cliente de consola (`atm-desktop`) para gestionar de forma robusta las peticiones HTTP/1.1 hacia el servicio web (`atm-web-service`). Facilita la construcción limpia de llamadas, el manejo eficiente de cabeceras obligatorias (como el `Idempotency-Key`) y la captura de códigos de error de manera síncrona.
* **`org.junit.jupiter:junit-jupiter` (Versión 5.14.4 - Scope: `test`):** 
  Framework estándar de pruebas unitarias e integración (JUnit 5). Permite estructurar la suite de validación de reglas de negocio, manejo de cuentas y servicios de idempotencia de forma limpia mediante anotaciones modernas (`@Test`, `@BeforeEach`, etc.).
* **`org.mockito:mockito-core` (Versión 4.11.0 - Scope: `test`):** 
  Librería de *mocking* utilizada para aislar componentes y simular comportamientos en las pruebas unitarias, facilitando la verificación de lógica compleja sin acoplamiento a implementaciones reales.
* **Plugins de Maven (`maven-enforcer-plugin` y `maven-shade-plugin`):**
  * *`maven-enforcer-plugin` (v3.4.1):* Garantiza que el proyecto sea compilado y ejecutado exclusivamente bajo el entorno requerido (JDK 1.8), previniendo desvíos de versión.
  * *`maven-shade-plugin` (v3.5.3):* Utilizado para empaquetar la aplicación en un formato ejecutable (*fat jar* / *uber-jar*), configurando el `ManifestResourceTransformer` para apuntar a la clase principal (`com.atm.web_service.Main`), facilitando la ejecución directa del servicio web desde la terminal.