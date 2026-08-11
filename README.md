# ⚡ Avengers Shop — Backend API 🛡️

¡Hola y bienvenid@ a **Avengers Shop**!
Este proyecto nace como el motor backend para una tienda online y sistema de gestión de comandas inspirado en el universo de Marvel. Lo hemos desarrollado en equipo con muchísima ilusión, cuidando cada detalle de la arquitectura para que sea limpio, robusto y preparado para crecer.

---

## 🚀 ¿Qué hace especial a este proyecto?

* **Solución E-Commerce Completa:** Pensada para simular el flujo real de una tienda de merchandising y atención en punto de venta/comandas.
* **Estructura Modular y Limpia:** Separación clara de responsabilidades entre Controladores, Servicios, Repositorios y DTOs.
* **Respuestas Paginadas para Frontend:** Integración de `Pageable` en endpoints clave (`Page<ProductoDTO>`), devolviendo metadatos de paginación (`totalPages`, `totalElements`, `page`, `size`) para facilitarle 100% el trabajo a la capa cliente.
* **Estructura de DTOs Anidados:** Consultas optimizadas donde las categorías incluyen directamente el listado de sus productos.
* **Gestión de Pedidos en Tiempo Real:** Flujo completo desde que se crea un carrito, se añaden o personalizan productos, hasta que pasa por pantalla y se entrega.
* **Seguridad y Datos Protegidos:** Control automático de duplicados, transacciones atómicas (`@Transactional`) y filtrado condicional directamente en MySQL.
* **Toque Épico:** ¡Le hemos integrado un banner personalizado de Marvel en la consola que saluda cada vez que se arranca la aplicación! *(Consejo: Poned la banda sonora de Los Vengadores para mayor epicidad)* 🎶

---

## 🛠️ Tecnologías utilizadas

* **Java 21**
* **Spring Boot 3.x** 🍃
* **Spring Data JPA / Hibernate**
* **MySQL** 🐬
* **Jakarta Validation**
* **Lombok**
* **Swagger UI / Springdoc OpenAPI**

---

## ⚙️ Requisitos previos

* Java JDK 21 instalado.
* MySQL Server ejecutándose en local o servidor.
* Maven instalado.

---

## 📖 Documentación Interactiva & Postman

* 🌐 **Swagger UI:** Una vez arrancada la aplicación, puedes probar de forma interactiva y visual todos los endpoints accediendo a:  
  👉 `http://localhost:8080/doc/swagger-ui/index.html`
* 📬 **Colección Postman:** En la raíz del proyecto se incluye el archivo de colección para Postman listo para importar.

---

## 📌 Resumen de Endpoints REST

El servidor se iniciará por defecto en `http://localhost:8080`

### 🖥️ Terminales (`/api/terminales`)
* `GET /api/terminales` - Listar terminales de venta.
* `POST /api/terminales` - Crear nueva terminal.

### 🏷️ Categorías (`/api/categorias`)
* `GET /api/categorias` - Listar todas las categorías (incluye la lista de productos anidados en cada una).
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
* `GET /api/pedidos/codigo/{codigo}` - Consultar la información completa de un pedido mediante su código.
* `GET /api/pedidos/pantalla` - Vista resumida de comandas para pantallas del local.

---

## 👥 Autores y Créditos

* **Pol Bastida Gonzalez** ⚡
* **Linneth Rodrigues** 🚀