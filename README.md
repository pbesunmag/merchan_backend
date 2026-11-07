# ⚡ Avengers Shop — Backend API 🛡️

¡Hola y bienvenid@ a Avengers Shop! Este proyecto nace como el motor backend para una tienda online y sistema de gestión de comandas inspirado en el universo de Marvel. Lo hemos desarrollado en equipo con muchísima ilusión, cuidando cada detalle de la arquitectura para que sea limpio, robusto y preparado para crecer.

---

### 📺 Demostración en Vídeo
Puedes ver la demostración interactiva completa del funcionamiento del sistema y flujo operativo en el siguiente enlace:  
👉 [Ver Demostración en Google Drive](https://drive.google.com/file/d/1WPxUktuc9ovxIFpQScT5KjU1H2rgbBwV/view?usp=sharing)

---

## 🚀 Características Principales
- Solución E-Commerce Completa: Pensada para simular el flujo real de una tienda de merchandising y atención en punto de venta/comandas.
- Estructura Modular y Limpia: Separación clara de responsabilidades entre Controladores, Servicios, Repositorios y DTOs.
- Respuestas Paginadas para Frontend: Integración de Pageable en endpoints clave (Page<ProductoDTO>), devolviendo metadatos de paginación (totalPages, totalElements, page, size) para facilitarle 100% el trabajo a la capa cliente.
- Estructura de DTOs Anidados: Consultas optimizadas donde las categorías incluyen directamente el listado de sus productos.
- Gestión de Pedidos en Tiempo Real: Flujo completo desde que se crea un carrito, se añaden o personalizan productos, hasta que pasa por pantalla y se entrega.
- Seguridad y Datos Protegidos: Control automático de duplicados, transacciones atómicas (@Transactional) y filtrado condicional directamente en MySQL.
- Toque Épico: ¡Le hemos integrado un banner personalizado de Marvel en la consola que saluda cada vez que se arranca la aplicación! (Consejo: Poned la banda sonora de Los Vengadores para mayor epicidad) 🎶

---

## 🛠️ Tecnologías Utilizadas
- Java 21
- Spring Boot 3.x 🍃
- Spring Data JPA / Hibernate
- MySQL 8.0+ 🐬
- Jakarta Validation
- Lombok
- Swagger UI / Springdoc OpenAPI

---

## 🏗️ Estructura del Proyecto

El código está estructurado en capas con responsabilidades delimitadas:

src/main/java/avengersshop/merchan_backend/
├── configs/          # Configuraciones globales (OpenAPI)
├── controllers/      # Controladores REST (Exposición de Endpoints)
├── dto/
│   ├── request/      # DTOs de entrada con validaciones Jakarta
│   └── response/     # DTOs de salida enriquecidos y paginados
├── exceptions/       # Excepciones personalizadas y GlobalExceptionHandler
├── models/           # Entidades JPA y Enums de dominio
├── repositories/     # Interfaces Spring Data JPA
└── services/         # Lógica de negocio y transaccionalidad (@Transactional)

---

## 🗄️ Modelo de Datos y Reglas de Negocio

### Entidades Clave
1. Terminal: Puntos de venta autorizados que emiten pedidos.
2. Categoria: Agrupación lógica de productos. Sus respuestas DTO incluyen la lista anidada de productos.
3. Producto (y ProductoTalla): Artículos del catálogo con stock, tallas, personalización y borrado lógico (activo).
4. Pedido: Comanda con terminal de origen, código único autogenerado y estado (EstadoPedido).
5. PedidoProducto (tabla pedido_producto): Línea del pedido. Congela el precioUnitario y guarda:
    - talla (VARCHAR(10))
    - texto_personalizado (VARCHAR(500))
    - variante (VARCHAR(100)): variante elegida (modelo, color, héroe…). Es opcional y viaja en el checkout (AgregarProductoPedidoDTO.variante) y en LineaPedidoDTO.variante, por lo que llega a la vista de preparación.
6. MovimientoCaja (tabla movimientos_caja): Libro contable. Cada apunte guarda tipo (FONDO_INICIAL, VENTA, DEVOLUCION, DIFERENCIA_CAMBIO), importe y saldo_resultante.

El esquema se actualiza solo (spring.jpa.hibernate.ddl-auto=update): la columna variante se crea al arrancar.

### Flujo de caja
- POST /api/pedidos/checkout crea el pedido en CREADO y descuenta stock. No registra movimiento de caja.
- Al cambiar el estado a PAGO_ACEPTADO (PATCH /api/pedidos/{id}/estado) se asienta un movimiento VENTA por el total del pedido (saldo anterior + total). Si ya hay una venta vigente para el pedido, no se duplica.
- Cancelar el pedido o revertirlo a CREADO genera un movimiento DEVOLUCION que anula la venta vigente.
- Pedidos heredados sin pago registrado asientan la venta al pasar a ENTREGADO.
- Las devoluciones y cambios de postventa generan DEVOLUCION y DIFERENCIA_CAMBIO.
- El saldo actual se consulta en GET /api/caja/metricas?periodo=... (campo saldoActualCaja). Sin apuntes, la caja parte de un fondo base de 500 €.

---

## ⚙️ Requisitos Previos e Instalación

### Requisitos
- Java JDK 21 instalado.
- MySQL Server 8.0+ ejecutándose localmente.
- Git configurado con clave SSH.
- Maven 3.8+ instalado.

### Pasos de instalación

1. Clonar el repositorio mediante SSH:
   git clone git@github.com:pbesunmag/merchan_backend.git
   cd merchan_backend

2. Asegurar la ejecución de MySQL: Asegúrate de que tu servicio local de MySQL esté activo en el puerto 3306. (No es necesario crear manualmente la base de datos, Spring Boot la creará automáticamente al arrancar si no existe gracias al parámetro createDatabaseIfNotExist=true).

3. Configurar las Variables de Entorno (.env): Crea un archivo llamado .env en la raíz del proyecto (al mismo nivel que pom.xml) con la configuración de tu entorno local:
   SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/merchan_backend?createDatabaseIfNotExist=true&serverTimezone=UTC
   SPRING_DATASOURCE_USERNAME=root
   SPRING_DATASOURCE_PASSWORD=tu_contraseña_de_mysql

4. Compilar y arrancar la aplicación:
   En Linux/macOS:
   ./mvnw spring-boot:run

En Windows:
mvnw.cmd spring-boot:run

(También puedes ejecutar la clase principal MerchanBackendApplication.java directamente desde IntelliJ IDEA)

---

## 🔑 Configuración de Variables de Entorno

El proyecto está configurado para importar de forma obligatoria el archivo .env desde la raíz a través de application.properties:

spring.config.import=file:.env[.properties]

Asegúrate de que las credenciales coincidan con las de tu usuario local de MySQL.

---

## 📖 Documentación Interactiva & Postman

- 🌐 Swagger UI: Una vez arrancada la aplicación, puedes probar de forma interactiva y visual todos los endpoints accediendo a:  
  👉 http://localhost:8080/doc/swagger-ui.html
- 📬 Colección Postman: En la raíz del proyecto se incluye el archivo JSON con la colección de Postman lista para importar.

---

## 📌 Resumen de Endpoints REST

El servidor se inicia por defecto en http://localhost:8080.

### 🖥️ Terminales (/api/terminales)
- GET /api/terminales — Listar terminales.
- POST /api/terminales — Crear terminal.

### 🏷️ Categorías (/api/categorias)
- GET /api/categorias — Listar categorías con sus productos.
- GET /api/categorias/{id} — Detalle de una categoría.
- POST /api/categorias — Crear categoría.
- PUT /api/categorias/{id} — Actualizar categoría.

### 📦 Productos (/api/productos)
- GET /api/productos — Listado paginado (page, size, sort, idCategoria, activos).
- GET /api/productos/{id} — Detalle.
- GET /api/productos/{id}/tallas — Stock por talla.
- POST /api/productos — Crear producto.
- POST /api/productos/{id}/tallas — Añadir talla.
- PUT /api/productos/{id} — Actualizar producto.
- PUT /api/productos/{id}/tallas/{talla} — Actualizar talla.
- PATCH|PUT /api/productos/{id}/stock — Ajustar stock.
- PATCH|PUT /api/productos/{id}/tallas/{talla}/stock — Ajustar stock de una talla.
- PATCH /api/productos/{id}/desactivar — Borrado lógico.
- PATCH /api/productos/{id}/reactivar — Reactivar producto.

### 📋 Pedidos (/api/pedidos)
- POST /api/pedidos/checkout — Checkout atómico: pedido + líneas (con variante) en una transacción. Deja el pedido en CREADO sin mover caja.
- POST /api/pedidos — Crear un pedido vacío.
- POST /api/pedidos/{pedidoId}/productos — Añadir una línea (admite talla, textoPersonalizado, variante).
- DELETE /api/pedidos/{pedidoId}/productos/{productoId} — Eliminar una línea.
- PATCH /api/pedidos/{pedidoId}/estado — Cambiar estado. PAGO_ACEPTADO registra la venta en caja.
- GET /api/pedidos — Listado paginado (filtro por estado).
- GET /api/pedidos/codigo/{codigo} — Consulta por código.
- GET /api/pedidos/pantalla — Vista resumida para pantallas del local.
- POST /api/pedidos/{id}/devolucion — Devolución total de un pedido entregado.
- POST /api/pedidos/{id}/devolucion-linea — Devolución de una línea.
- POST /api/pedidos/{id}/cambio-producto — Cambio de artículos.

### 💰 Caja (/api/caja)
- GET /api/caja/metricas?periodo={dia|mes|anio} — Saldo actual, ingresos, reembolsos, beneficio y transacciones.

### 📊 Estadísticas (/api/estadisticas)
- GET /api/estadisticas/producto-mas-vendido
- GET /api/estadisticas/productos-mas-vendidos

### 🧾 Informes (/api/informes)
- GET /api/informes/ventas/csv
- GET /api/informes/ventas/completo

### 🔌 WebSocket
- Endpoint /ws (STOMP). Topic /topic/productos-stock: publica el stock actualizado de un producto tras cada commit.

---

## 🛡️ Manejo de Excepciones

La API cuenta con una capa centralizada de captura de errores (GlobalExceptionHandler) mediante @RestControllerAdvice. Las respuestas de error siguen una estructura JSON homogénea:

- HTTP 400 Bad Request: Errores de validación en DTOs (@Valid), parámetros con tipos incorrectos, JSON mal formado o peticiones que incumplen reglas de negocio.
- HTTP 404 Not Found: Recurso no encontrado (Producto, Categoría, Pedido o Terminal inexistente).
- HTTP 405 Method Not Allowed: Intento de consumo de un endpoint utilizando un método HTTP no permitido para dicha ruta.
- HTTP 409 Conflict: Conflictos de estado o integridad en base de datos, como el registro de nombres duplicados o eliminación de datos con elementos asociados.
- HTTP 500 Internal Server Error: Excepciones inesperadas no controladas.

---

## 👥 Autores y Créditos
- Pol Bastida González ⚡
- Linneth Rodrigues 🚀