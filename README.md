# ⚡ Avengers Shop — Backend API 🛡️

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D.svg?style=for-the-badge&logo=swagger)](http://localhost:8080/doc/swagger-ui/index.html)

¡Hola y bienvenid@ a **Avengers Shop**!
Este proyecto nace como el motor backend para una tienda online y sistema de gestión de comandas inspirado en el universo de Marvel. Lo hemos desarrollado en equipo con muchísima ilusión, cuidando cada detalle de la arquitectura para que sea limpio, robusto y preparado para crecer.

---

## 🚀 Características Principales

* **Solución E-Commerce Completa:** Pensada para simular el flujo real de una tienda de merchandising y atención en punto de venta/comandas.
* **Estructura Modular y Limpia:** Separación clara de responsabilidades entre Controladores, Servicios, Repositorios y DTOs.
* **Respuestas Paginadas para Frontend:** Integración de `Pageable` en endpoints clave (`Page<ProductoDTO>`), devolviendo metadatos de paginación (`totalPages`, `totalElements`, `page`, `size`) para facilitarle 100% el trabajo a la capa cliente.
* **Estructura de DTOs Anidados:** Consultas optimizadas donde las categorías incluyen directamente el listado de sus productos.
* **Gestión de Pedidos en Tiempo Real:** Flujo completo desde que se crea un carrito, se añaden o personalizan productos, hasta que pasa por pantalla y se entrega.
* **Seguridad y Datos Protegidos:** Control automático de duplicados, transacciones atómicas (`@Transactional`) y filtrado condicional directamente en MySQL.
* **Toque Épico:** ¡Le hemos integrado un banner personalizado de Marvel en la consola que saluda cada vez que se arranca la aplicación! *(Consejo: Poned la banda sonora de Los Vengadores para mayor epicidad)* 🎶

---

## 🛠️ Tecnologías Utilizadas

* **Java 21**
* **Spring Boot 3.x** 🍃
* **Spring Data JPA / Hibernate**
* **MySQL** 🐬
* **Jakarta Validation**
* **Lombok**
* **Swagger UI / Springdoc OpenAPI**

---

## 🏗️ Estructura del Proyecto

El código está estructurado en capas con responsabilidades delimitadas:

```text
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
```

---

## 🗄️ Modelo de Datos y Reglas de Negocio

**Entidades Clave**

1. `Terminal`: Representa los puntos de venta autorizados que emiten pedidos.
2. `Categoria`: Agrupación lógica de productos. Sus respuestas DTO incluyen la lista anidada de productos pertenecientes a ella.
3. `Producto`: Artículos del catálogo. Soportan personalización mediante texto libre y borrado lógico.
4. `Pedido`: Registra la comanda, terminal de origen, código único autogenerado y el estado en el flujo de trabajo (`EstadoPedido`).
5. `PedidoProducto`: Entidad intermedia que gestiona la relación muchos-a-muchos entre Pedido y Producto, congelando el `precioUnitario` al momento de la compra.

---

## ⚙️ Requisitos Previos e Instalación

**Requisitos**

* **Java JDK 21** instalado.
* **MySQL Server 8.0+** ejecutándose en localmente.
* **Git** configurado con clave SSH
* **Maven 3.8+** instalado.

**Pasos de instalación**

1. **Clonar el repositorio mediante SSH:**

*Bash*
```text
git clone git@github.com:pbesunmag/merchan_backend.git
cd merchan_backend
```
2. **Asegurar la ejecución de MySQL:**
   Asegúrate de que tu servicio local de MySQL esté activo en el puerto 3306. (No es necesario crear manualmente la base de datos, Spring Boot la creará automáticamente al arrancar si no existe gracias al parámetro `createDatabaseIfNotExist=true`).


3. **Configurar las Variables de Entorno (`.env`):**
Crea un archivo llamado `.env` en la raíz del proyecto (al mismo nivel que `pom.xml`) con la configuración de tu entorno local:

*Fragmento de código*
```text
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/merchan_backend?createDatabaseIfNotExist=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=tu_contraseña_de_mysql
```

4. **Compilar y arrancar la aplicación:**

*Bash*
```text
./mvnw spring-boot:run
```

(En *Windows* puedes usar `mvnw.cmd spring-boot:run` o ejecutar la clase principal `MerchanBackendApplication.java` directamente desde tu IDE).

---

## 🔑 Configuración de Variables de Entorno

El proyecto está configurado para importar de forma obligatoria el archivo `.env` desde la raíz a través de `application.properties`:

*Properties*
```text
spring.config.import=file:.env[.properties]
```

Asegúrate de que las credenciales coincidan con las de tu usuario local de MySQL.

---

## 📖 Documentación Interactiva & Postman

* 🌐 **Swagger UI:** Una vez arrancada la aplicación, puedes probar de forma interactiva y visual todos los endpoints accediendo a:  
  👉 `http://localhost:8080/doc/swagger-ui.html
* 📬 **Colección Postman:** En la raíz del proyecto se incluye el archivo de colección para Postman listo para importar.

---

## 📌 Resumen de Endpoints REST

El servidor se iniciará por defecto en `http://localhost:8080`

### 🖥️ Terminales (`/api/terminales`)
* `GET /api/terminales` - Listar terminales de venta.
* `POST /api/terminales` - Crear nueva terminal de venta.

### 🏷️ Categorías (`/api/categorias`)
* `GET /api/categorias` - Listar todas las categorías con sus productos anidados.
* `GET /api/categorias/{id}` - Obtener el detalle de una categoría concreta por ID con sus productos.
* `POST /api/categorias` - Crear nueva categoría.

### 📦 Productos (`/api/productos`)
* `GET /api/productos` - Listar productos paginados (admite `page`, `size`, `sort`, filtro opcional por `idCategoria` y por estado `activos`).
* `GET /api/productos/{id}` - Obtener los detalles de un producto específico por su ID.
* `POST /api/productos` - Crear nuevo producto.
* `PUT /api/productos/{id}` - Actualizar un producto existente.
* `PATCH /api/productos/{id}/desactivar` - Desactivar/borrado lógico de un producto.
* `PATCH /api/productos/{id}/reactivar` - Reactivar un producto previamente desactivado.

### 📋 Pedidos (`/api/pedidos`)
* `POST /api/pedidos` - Iniciar un nuevo pedido desde terminal generando un código automático.
* `POST /api/pedidos/{pedidoId}/productos` - Añadir un producto con opción de texto personalizado.
* `DELETE /api/pedidos/{pedidoId}/productos/{productoId}` - Eliminar producto del pedido.
* `PATCH /api/pedidos/{pedidoId}/estado` - Cambiar el estado del pedido en la cadena de montaje/pago.
* `GET /api/pedidos` - Listar todos los pedidos (admite filtro por `estadoPedido`).
* `GET /api/pedidos/codigo/{codigo}` - Consultar la información completa de un pedido mediante su código único.
* `GET /api/pedidos/pantalla` - Vista resumida para pantallas del local.

---

## 🛡️ Manejo de Excepciones

La API cuenta con una capa centralizada de captura de errores (`GlobalExceptionHandler`) mediante `@RestControllerAdvice`. Las respuestas de error siguen una estructura JSON homogénea:

* **HTTP 400 Bad Request:** Errores de validación en DTOs (`@Valid`), datos duplicados o parámetros mal formados.

* **HTTP 404 Not Found:** Recurso no encontrado (Producto, Categoría, Pedido o Terminal inexistente).

* **HTTP 500 Internal Server Error:** Excepciones inesperadas no controladas.

---

## 👥 Autores y Créditos

* **Pol Bastida Gonzalez** ⚡
* **Linneth Rodrigues** 🚀