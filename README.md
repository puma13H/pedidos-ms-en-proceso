# pedidos-ms

Microservicio autónomo de órdenes para el proyecto `pagatu`. Gestiona el ciclo de vida de las órdenes; no mantiene categorías, productos ni clientes. La referencia a un producto debe resolverse por API entre microservicios cuando esa integración exista, nunca leyendo la base de datos del catálogo.

## Stack

- Java 21 y Spring Boot 4.0.7
- Spring Web MVC, Validation y Spring Data JPA
- PostgreSQL 16 y Flyway
- SpringDoc OpenAPI, Actuator y MDC con `X-Trace-ID`
- Maven Wrapper para DEV y Docker Compose para PROD local

## Estructura

```text
src/main/java/pe/edu/upeu/orden/
  controller/   API REST
  dto/          contrato de entrada y salida
  entity/       modelo persistente
  exception/    errores HTTP centralizados
  filter/       trazabilidad por request
  mapper/       conversion DTO-entidad
  repository/   acceso JPA
  service/      reglas de aplicación
src/main/resources/db/migration/V1__create_orden_tables.sql
```

## DEV

Requisito: JDK 21, Docker Desktop y PowerShell.

```powershell
# PostgreSQL DEV: localhost:15433 -> 5432
Docker compose -f compose-dev.yml up -d
Docker exec -it pagatu-postgres-pedidos-dev psql -U pagatu -d pagatu_orden_db -c "SELECT current_database();"

# Aplicación en localhost:8080
.\mvnw.cmd spring-boot:run
```

Comprobaciones:

```powershell
Invoke-RestMethod http://localhost:8080/saludo
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/metrics
Start-Process http://localhost:8080/swagger-ui/index.html
```

La segunda instancia usa el mismo artefacto y la misma BD, pero otro puerto:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
```

La aplicación es stateless: el estado de las órdenes vive en PostgreSQL, no en la memoria de una instancia. Por eso se pueden ejecutar copias idénticas en `8080` y `8081`; posteriormente un Gateway podrá distribuir el tráfico.

## API REST

`OrdenRequest` valida nombre del cliente, estado, longitud de textos y total no negativo.

```powershell
$body = '{"clienteNombre":"Ana Torres","descripcion":"Orden de matrícula","total":350.00,"estado":"PENDIENTE"}'
$orden = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/ordenes -ContentType application/json -Body $body
$orden
Invoke-RestMethod http://localhost:8080/api/v1/ordenes
Invoke-RestMethod http://localhost:8080/api/v1/ordenes/$($orden.id)

$update = '{"clienteNombre":"Ana Torres","descripcion":"Orden confirmada","total":350.00,"estado":"CONFIRMADA"}'
Invoke-RestMethod -Method Put -Uri http://localhost:8080/api/v1/ordenes/$($orden.id) -ContentType application/json -Body $update
Invoke-RestMethod -Method Delete -Uri http://localhost:8080/api/v1/ordenes/$($orden.id)
```

Casos de defensa:

```powershell
# 400: clienteNombre vacio o total negativo
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/ordenes -ContentType application/json -Body '{"clienteNombre":"","total":-1,"estado":"PENDIENTE"}'

# 404: orden inexistente
Invoke-RestMethod http://localhost:8080/api/v1/ordenes/9999

# Trace ID generado por el servicio y devuelto en la respuesta
Invoke-WebRequest http://localhost:8080/api/v1/ordenes -Headers @{"X-Trace-ID"="evidencia-s01-orden"}
```

Swagger: `http://localhost:8080/swagger-ui/index.html`.

## PostgreSQL y Flyway

```powershell
Docker exec -it pagatu-postgres-pedidos-dev psql -U pagatu -d pagatu_orden_db -c "\dt"
Docker exec -it pagatu-postgres-pedidos-dev psql -U pagatu -d pagatu_orden_db -c "\d ordenes"
Docker exec -it pagatu-postgres-pedidos-dev psql -U pagatu -d pagatu_orden_db -c "SELECT * FROM ordenes;"
```

Flyway aplica `V1__create_orden_tables.sql` una sola vez. Hibernate usa `ddl-auto: validate`, por lo que no crea ni modifica el esquema.

## PROD local opcional

```powershell
Docker compose up -d --build --scale pedidos-ms=2
Docker compose ps
Docker run --rm --network pagatu-pedidos-int curlimages/curl:8.10.1 -s http://pedidos-ms:8080/actuator/health
Docker compose logs --tail=80 pedidos-ms
Docker compose down
```

En PROD local el microservicio escucha en el puerto interno `8080` y no publica un puerto host. El cliente de prueba entra por la red Docker; un Gateway será el punto de acceso externo en una sesión posterior. PostgreSQL sí queda disponible en `localhost:25433` para inspección.

## Compilación y pruebas

```powershell
.\mvnw.cmd -q test
.\mvnw.cmd clean package -DskipTests
```

El proyecto requiere JDK 21. Verificar con `java --version` antes de ejecutar.

## Evidencia S01

Cada captura debe incluir el reloj del sistema y el usuario/perfil visible, sin recortar. No se incluyen capturas ficticias en el repositorio; deben tomarse durante la ejecución real.

1. **Dominio:** explicar que `pedidos-ms` gestiona pedidos y su estado, separado del catálogo.
2. **Persistencia:** mostrar `docker ps`, logs de Flyway y consultas `psql`.
3. **REST:** mostrar `mvnw spring-boot:run`, CRUD por PowerShell y Swagger.
4. **Escalamiento:** mostrar `/actuator/health` y `/actuator/metrics` en `8080` y `8081`.
5. **Reproducibilidad:** mostrar este README, `compose-dev.yml`, wrapper y, opcionalmente, `compose.yml`.

## Hallazgo técnico

El primer intento de compilación encontró un POM vacío en la caché local de Maven (`testcontainers-bom`). Se diagnosticó porque Maven falló antes de leer el código del proyecto. Se eliminó únicamente ese artefacto corrupto y la compilación continuó; el aprendizaje es distinguir errores de caché/herramientas de errores del microservicio.
