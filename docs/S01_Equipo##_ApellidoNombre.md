# S01 - Evidencia individual de pedidos-ms

> Reemplazar los campos entre corchetes y exportar esta página a `S01_Equipo##_ApellidoNombre.pdf` desde MkDocs o una herramienta equivalente. Cada captura debe conservar reloj del sistema y usuario/perfil visible.

## Datos del estudiante

- Nombre: Frank Alexander
- Equipo: En proceso
- Sesión: S01 - Construcción de un servicio base para un sistema distribuido
- Rol o aporte: Implementación individual de `pedidos-ms`, CRUD, persistencia y pruebas.
- GitHub: https://github.com/puma13H/pedidos-ms-en-proceso

## Evidencia técnica

### 1. Microservicio delimitado

Caso demostrado: una tienda registra el pedido de Juan Perez por una laptop Lenovo y un mouse, por un total de S/ 2850.00. `pedidos-ms` gestiona el cliente del pedido, la descripcion, el total, el estado y la fecha de creacion. `pagatu-catalogo-ms` mantiene los productos y categorias; separar ambos dominios evita que `pedidos-ms` lea las tablas del catalogo.

Estados usados en la evidencia: `PENDIENTE`, `CONFIRMADA`, `PREPARANDO`, `ENVIADA`, `ENTREGADA` y `CANCELADA`.

**Captura 1:** [insertar captura del arbol del proyecto y del codigo de `Orden`]

### 2. PostgreSQL y Flyway

La migracion `V1__create_orden_tables.sql` crea `ordenes` con `cliente_nombre`, `descripcion`, `total`, `estado` y `fecha_creacion`. La aplicacion usa `ddl-auto: validate`, por lo que Flyway define el esquema y Hibernate solo lo comprueba.

**Captura 2:** [insertar `docker ps` mostrando PostgreSQL en `15433`]

**Captura 3:** [insertar log con `Successfully applied 1 migration` y consulta `psql`]

Consulta sugerida:

```powershell
docker exec -it pagatu-postgres-pedidos-dev psql -U pagatu -d pagatu_orden_db -c "SELECT id, cliente_nombre, descripcion, total, estado, fecha_creacion FROM ordenes;"
```

### 3. REST y Swagger

El CRUD se prueba con `/api/v1/ordenes`, no con `/api/ordenes`.

```powershell
$body = '{"clienteNombre":"Juan Perez","descripcion":"Laptop Lenovo + Mouse","total":2850.00,"estado":"PENDIENTE"}'
$orden = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/ordenes -ContentType "application/json" -Body $body
$orden

Invoke-RestMethod http://localhost:8080/api/v1/ordenes
Invoke-RestMethod http://localhost:8080/api/v1/ordenes/$($orden.id)

$update = '{"clienteNombre":"Juan Perez","descripcion":"Laptop Lenovo + Mouse","total":2850.00,"estado":"CONFIRMADA"}'
Invoke-RestMethod -Method Put -Uri http://localhost:8080/api/v1/ordenes/$($orden.id) -ContentType "application/json" -Body $update

Invoke-RestMethod -Method Delete -Uri http://localhost:8080/api/v1/ordenes/$($orden.id)
```

La evidencia debe mostrar la respuesta `201 Created` del POST, el cambio de estado a `CONFIRMADA` en el PUT y el registro actualizado en PostgreSQL antes del DELETE.

**Captura 4:** [insertar CRUD por PowerShell]

**Captura 5:** [insertar Swagger en `http://localhost:8080/swagger-ui/index.html` con `OrdenController`]

### 4. Health, métricas y escalamiento

Terminal 1:

```powershell
.\mvnw.cmd spring-boot:run
```

Terminal 2:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Luego probar ambas copias:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/metrics
Invoke-RestMethod http://localhost:8081/actuator/metrics
```

Resultado esperado en health: estado `UP` en ambos puertos. Las dos instancias son copias stateless y comparten la misma PostgreSQL; el estado de la orden no se guarda en la memoria de una instancia.

**Captura 6:** [insertar las dos terminales visibles, con `8080` y `8081`]

Resultados verificados durante la ejecucion:

```text
GET /actuator/health en 8080: UP
GET /actuator/health en 8081: UP
GET /actuator/metrics en 8080: HTTP 200
GET /actuator/metrics en 8081: HTTP 200
GET /swagger-ui/index.html en 8080: HTTP 200
GET /swagger-ui/index.html en 8081: HTTP 200
```

La orden `id=1`, `clienteNombre=Juan Perez`, `total=2850.00` y `estado=CONFIRMADA` se consulto correctamente desde ambas instancias y desde PostgreSQL.

### 5. Documentación reproducible

La ejecución está documentada en [README.md](../README.md), con comandos DEV, PostgreSQL, CRUD, Docker PROD y diagnóstico del hallazgo técnico.

## Error o hallazgo

Durante la primera prueba PROD, las instancias conectaban a PostgreSQL pero fallaban con `Schema validation: missing table [ordenes]`. El diagnostico mostro que no aparecia ningun evento de Flyway y `psql` devolvia `Did not find any relations`. Se agrego `spring-boot-starter-flyway` al `pom.xml`, se recreo el volumen de laboratorio y Flyway aplico V1 correctamente antes de que Hibernate validara el esquema.

## Reflexión técnica

Un microservicio reproducible reduce las diferencias entre equipos y ambientes. DEV debe permitir cambios rápidos con Maven Wrapper y PostgreSQL aislado. PROD local debe acercarse al empaquetado real mediante Docker. Ser stateless permite que varias instancias compartan el trabajo sin perder información local. Cada copia usa un puerto distinto durante la simulación local. En un entorno real, un Gateway o balanceador repartiría las solicitudes. La persistencia queda centralizada en la base propia del servicio y Flyway controla su evolución.

## Anexo: Feedback de la sesión

- Aprendizaje más importante: [respuesta]
- Punto más confuso: [respuesta]
- Pregunta para la siguiente clase: [respuesta]
- Nivel: [Entendido / Más o menos / Necesito ayuda]
- Cómo mejorar mi comprensión: [respuesta]
- Compromiso: [Muy Comprometido/a / Comprometido/a / Poco Comprometido/a]
- Satisfacción (1-10): [número]
