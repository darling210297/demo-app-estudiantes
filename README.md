# ISWZ2202 — App base (Sesión 4-6)

App de catálogo de productos (Spring Boot). Cada llamada a la API tiene una
latencia simulada a propósito.

## Requisitos

- Java 17+ únicamente. No necesitas Maven instalado: el proyecto trae el
  Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Ejecutar

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080`.

```bash
curl http://localhost:8080/api/productos
curl http://localhost:8080/api/productos/1
```

## Frontend

El frontend está en `frontend/index.html` (HTML + JS puro, sin build).
Debe servirse desde un servidor HTTP (no abrir el archivo con doble clic,
porque el origen `file://` es `null` y CORS lo bloquea):

```bash
# Opción 1: extensión Live Server de VS Code (puerto 5500)
# Opción 2: Python
cd frontend
python -m http.server 5500
```

Abrir `http://localhost:5500`.

## Endpoints

| Método | Ruta                   | Descripción                          |
|--------|------------------------|--------------------------------------|
| GET    | `/api/productos`       | Lista todos (cacheado)               |
| GET    | `/api/productos/{id}`  | Busca por id (cacheado por id)       |
| DELETE | `/api/cache`           | Vacía la caché                       |

Errores: id inexistente → `404`, id ≤ 0 → `400` (rechazado por el proxy).

## Diseño de la solución

```
Controller ──> ProductoService ──(@Cacheable)──> ProductoRepository (interfaz)
                                                    ▲              ▲
                                                    │              │
                                     ProductoRepositoryProxy ──> RepositorioProductoEnMemoria
                                           (@Primary, Proxy)         (RealSubject, 1.5 s)
```

### 1. Caché (`config/CacheConfig`, `service/ProductoService`)

- `@EnableCaching` activa la caché de Spring (en memoria, `ConcurrentMapCacheManager`).
- `@Cacheable("productos")` en `listarTodos()` y `@Cacheable(value = "producto", key = "#id")`
  en `buscarPorId()`: la primera llamada tarda ~1.5 s, las siguientes ~0 ms.
- `@CacheEvict` en `limpiarCache()` para invalidarla (`DELETE /api/cache`).
- La caché se ubica en el **service** (capa de negocio) y no en el controller, para
  que cualquier cliente del service se beneficie.
- Dato interesante: Spring implementa `@Cacheable` creando un **proxy dinámico**
  alrededor de `ProductoService`; por eso una llamada interna `this.metodo()` no pasa por la caché.

### 2. Patrón Proxy (`repository/ProductoRepositoryProxy`)

Implementación explícita del patrón Proxy (GoF, estructural):

| Rol          | Clase                            |
|--------------|----------------------------------|
| Subject      | `ProductoRepository`             |
| RealSubject  | `RepositorioProductoEnMemoria`   |
| Proxy        | `ProductoRepositoryProxy`        |
| Cliente      | `ProductoService`                |

El proxy implementa la misma interfaz y envuelve al repositorio real. Agrega, sin modificar
la clase real (principio Abierto/Cerrado):

- **Protección:** rechaza ids nulos o ≤ 0 sin consultar la fuente de datos.
- **Registro:** loguea cada acceso real y su duración.
- **Métrica:** cuenta los accesos reales (`getAccesosReales()`), lo que permite ver
  que con caché el repositorio solo se consulta una vez.

Spring inyecta el proxy gracias a `@Primary`; el proxy recibe el real con
`@Qualifier("repositorioProductoEnMemoria")`. El service no cambió: depende de la
abstracción (inversión de dependencias), así que el proxy es transparente para él.

En los logs se ve claramente: la primera llamada imprime `[PROXY] Acceso a fuente de datos ...`
y las siguientes no imprimen nada porque las resuelve la caché.

### 3. CORS (`config/CorsConfig`)

**Qué lo causa:** el navegador aplica la *Same-Origin Policy*. El frontend
(`http://localhost:5500`) y el backend (`http://localhost:8080`) tienen distinto
puerto, por lo tanto son **orígenes distintos**. El navegador envía la petición, pero
si la respuesta no trae `Access-Control-Allow-Origin` con el origen del front, bloquea
la respuesta y en consola aparece:

```
Access to fetch at 'http://localhost:8080/api/productos' from origin 'http://localhost:5500'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present...
```

(Postman o curl no tienen este problema porque CORS lo aplica solo el navegador.)

**Cómo se resolvió en desarrollo:** configuración global con `WebMvcConfigurer`
para `/api/**`, con los orígenes leídos de `application.properties`:

```properties
app.cors.allowed-origins=http://localhost:5500,http://127.0.0.1:5500,http://localhost:5173,http://localhost:3000
```

Alternativas válidas en desarrollo:
- `@CrossOrigin(origins = "http://localhost:5500")` en el controller (más simple, pero se repite por controller).
- Un *proxy de desarrollo* en el front (p. ej. `server.proxy` de Vite), así el navegador
  cree que todo es el mismo origen.

No se usa `allowedOrigins("*")` para no abrir la API a cualquier sitio.

## Pruebas

```bash
./mvnw test
```

`CacheYProxyTest` verifica que Spring inyecta el proxy, que la segunda llamada sale de
la caché (1 solo acceso real, < 100 ms) y que el proxy rechaza ids inválidos sin tocar la fuente.
