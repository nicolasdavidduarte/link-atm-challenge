# Link ATM Challenge - Servicio Web & Cliente Desktop

Este repositorio contiene la solución al "Challenge Técnico Producto ATM**. La solución consiste de un servicio web HTTP nativo y un cliente desktop que hace uso de la línea de comando de la consola (CLI), implementados en Java 8

---

## Arquitectura del Proyecto

El repositorio está estructurado como un proyecto multimódulo de Maven:

```text
link-atm-challenge/
├── pom.xml                   # POM padre con reglas de Maven Enforcer (Java 8)
├── atm-web-service/          # Microservicio backend HTTP + Registro de logs + Idempotencia
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/atm/web_service/
│       │   ├── controladores/ # ATMController (ruteo y desglosado HTTP)
│       │   ├── entidades/     # Tarjeta, Cuenta, TarjetaCuenta, TransactionLog, IdempotentRequest
│       │   ├── handlers/      # ErrorHandler
│       │   ├── interfaces/   # ApiOperation, IdempotentOperation
│       │   ├── repositorios/ # ATMRepository (persistencia en memoria)
│       │   ├── servicios/    # ATMService, IdempotencyService, TransactionLogService
│       │   ├── AtmHttpServer.java # Servidor HTTP nativo (com.sun.net.httpserver)
│       │   └── Main.java     # Entrypoint del servicio web
│       └── test/java/com/atm/web_service/ # Suite de tests unitarios (JUnit 5 + Mockito 3.x)
└── atm-desktop/              # Aplicación cliente por línea de comandos
    ├── pom.xml
    └── src/
        ├── main/java/com/atm/desktop/
        │   ├── AtmApiClient.java # Cliente HTTP (HttpURLConnection)
        │   └── Main.java         # CLI entrypoint y parseo de comandos
        └── test/java/com/atm/desktop/ # Tests del cliente (JUnit 5 + MockWebServer)
```

## Requisitos

* Java Development Kit (JDK): 1.8
* Apache Maven: 3.6.3

## Compilación y Empaquetado

Para compilar todo el proyecto y generar los JARs ejecutables, ejecutar en la raiz del repositorio
```bash
mvn clean package
```

## Instrucciones de ejecución

1. Iniciar el servicio web (Backend)
Ejecute el JAR del servicio web desde la raíz o desde el subdirectorio atm-web-service:

```bash
java -jar atm-web-service/target/atm-web-service-1.0-SNAPSHOT.jar
```

El servicio iniciará un servidor HTTP que escuchará en http://localhost:8080. Al ejecutar las operaciones, se creará/actualizará el archivo de auditoría log_transacciones.csv en el directorio de ejecución. El sistema al arrancar levanta un pequeño set de datos para probar.

2. Ejecutar la aplicación de consola (Desktop)
En otra terminal ejecute los comandos del cliente especificando la operación y sus argumentos.

###Métodos
A. Identificación (login)
java -jar atm-desktop/target/atm-desktop-1.0-SNAPSHOT.jar login <nroTarjeta>
Ejemplo:
```bash
java -jar atm-desktop/target/atm-desktop-1.0-SNAPSHOT.jar login 1234567890123456
```

B. Extracción de efectivo (extraer)
java -jar atm-desktop/target/atm-desktop-1.0-SNAPSHOT.jar extraer <nroTarjeta> <nroCuenta> <importe> [idempotencyKey]
** idempotencyKey opcional
Ejemplo:
```bash
java -jar atm-desktop/target/atm-desktop-1.0-SNAPSHOT.jar extraer 1234567890123456 1001 5000.00
```

C. Depósito de efectivo (depositar)
java -jar atm-desktop/target/atm-desktop-1.0-SNAPSHOT.jar depositar <nroTarjeta> <cbu> <importe> [idempotencyKey]
** idempotencyKey opcional
Ejemplo:
```bash
java -jar atm-desktop/target/atm-desktop-1.0-SNAPSHOT.jar depositar 1234567890123456 0110001330000012345678 10000.00
```

D. Consulta de saldo (saldo)
java -jar atm-desktop/target/atm-desktop-1.0-SNAPSHOT.jar saldo <nroTarjeta> <nroCuenta>
Ejemplo:
```bash
java -jar atm-desktop/target/atm-desktop-1.0-SNAPSHOT.jar saldo 1234567890123456 1001
```

## Evidencias

Un documento puede encontrarse en la carpeta evidencias que contiene casos de prueba e imágenes demostrando el funcionamiento del sistema

## Ejecución de Tests Unitarios

Para ejecutar la suite completa de pruebas unitarias en ambos módulos ejecutar sobre el raíz:
```bash
mvn test
```

### Cobertura de Pruebas:
* **`atm-web-service`**:
  * `ATMServiceTest`: Reglas de negocio (login, validación de tarjetas activas/inactivas, saldos e importes).
  * `IdempotencyServiceTest`: Tests de concurrencia multihilo (`ExecutorService` + `CountDownLatch`), detección de llaves duplicadas, bloqueo en procesamiento y rechazo de hashes/rutas inconsistentes.
  * `ATMControllerTest`: Verificación de ruteo y desglosado de parámetros.
  * `CuentaTest` & `TransactionLogTest`: Reglas de entidad, inmutabilidad y formateo del archivo CSV.
  * `TransactionLogServiceTest`: Escritura thread-safe en disco.
* **`atm-desktop`**:
  * `AtmApiClientTest`: Mocking del servidor HTTP vía `MockWebServer` para validar headers (`Idempotency-Key`), JSON payloads y códigos de respuesta/error.

## Patrones y Soluciones Destacadas

1. **Garantía de Idempotencia:** Las operaciones (`extraer` y `depositar`) soportan el envío del header `Idempotency-Key`. Este es opcional y de no enviarse una key es generada aleatóreamente, esto con el propósito del correcto funcionamiento del sistema y facilitar los tests.  El backend utiliza `IdempotencyService` con `ConcurrentHashMap` y hashes `SHA-256` del payload para evitar ejecuciones duplicadas por reintentos de red o fallos momentáneos.
2. **Seguridad Concurrente:** Bloques de sincronización explícitos (`synchronized`) en operaciones de saldos (`Cuenta`) y escrituras de auditoría en disco (`TransactionLogService`) para evitar condiciones de carrera (*race conditions*).
3. **Auditoría Transaccional:** Registro persistente en `log_transacciones.csv` con trazas formateadas de cada intento (exitoso o fallido), incluyendo identificador único de transacción (`TX-<timestamp>`), número de tarjeta, cuenta/CBU, tipo de operación e importe.
