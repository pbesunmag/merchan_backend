# ⚡ Avengers Shop — Backend API 🛡️

¡Hola y bienvenid@ a **Avengers Shop**! 

Este proyecto nace como el motor backend para una tienda online y sistema de gestión de comandas inspirado en el universo de Marvel. 
Lo hemos desarrollado **en equipo con muchísima ilusión**, cuidando cada detalle de la arquitectura para que sea limpio, robusto y preparado para crecer.

---

## 🚀 ¿Qué hace especial a este proyecto?

No es solo una API de prueba: es una solución completa pensada para simular el flujo real de una tienda de merchandising y atención en punto de venta.

* **Estructura modular y limpia:** Separación clara de responsabilidades entre Controladores, Servicios, Repositorios y DTOs.
* **Gestión de pedidos en tiempo real:** Flujo completo desde que se crea un carrito, se añaden o personalizan productos, hasta que pasa por pantalla y se entrega.
* **Seguridad y datos protegidos:** Control automático de duplicados, transacciones atómicas (`@Transactional`) y filtrado inteligente directamente en MySQL.
* **Toque épico:** ¡Le hemos integrado un banner personalizado de Marvel en la consola que saluda cada vez que se arranca la aplicación! (Consejo: Poned la banda sonora de vengadores para mayor epicidad)

---

## 🛠️ Tecnologías que hemos utilizado

* **Java 21**
* **Spring Boot 4.1.0** 🍃
* **Spring Data JPA / Hibernate**
* **MySQL** 🐬
* **Jakarta Validation**
* **Lombok**
* **Swagger UI / Springdoc OpenAPI**

---

## ⚙️ Requisitos previos

* **Java JDK 21** instalado.
* **MySQL Server** ejecutándose en local o servidor.
* **Maven** instalado

---

## 📖 Documentación Interactiva & Postman

* **🌐 Swagger UI:** Una vez arrancada la aplicación, puedes probar interactiva y visualmente todos los endpoints accediendo a *http://localhost:8080/swagger-ui.html*
* **📬 Colección Postman:** En la raíz del proyecto se incluye el archivo de colección para Postman listo para importar.

---

## 📌 Resumen de Endpoints REST

El servidor se iniciará por defecto en *http://localhost:8080*

**🖥️ Terminales** (*/api/terminales*)
* *GET /api/terminales* - Listar terminales de venta.
* *POST /api/terminales* - Crear nueva terminal.

**🏷️ Categorías** (*/api/categorias*)
* *GET /api/categorias* - Listar todas las categorías.
* *POST /api/categorias* - Crear nueva categoría.

**📦 Productos** (*/api/productos*)
* *GET /api/productos* - Listar productos (admite filtros por *activos*, *idCategoría* y ordenación por precio).
* *POST /api/productos* - Crear nuevo producto.
* *PUT /api/productos/{id}* - Actualizar un producto existente.
* *PATCH /api/productos/{id}/desactivar* - Desactivar/borrado lógico de un producto.
* *PATCH /api/productos/{id}/reactivar* - Reactivar un producto previamente desactivado.

**📋 Pedidos**
* *POST /api/pedidos* - Iniciar un nuevo pedido desde terminal generando un código automático.
* *POST /api/pedidos/{pedidoId}/productos* - Añadir un producto con opción de texto personalizado.
* *DELETE /api/pedidos/{pedidoId}/productos/{productoId}* - Eliminar producto del pedido.
* *PATCH /api/pedidos/{pedidoId}/estado* - Cambiar el estado del pedido.
* *GET /api/pedidos?estado={ESTADO}* - Listar todos los pedidos o filtrados por estado.
* *GET /api/pedidos/pantalla* - Vista resumida de pedidos para pantallas del local.

---

## 👥 Autores y Créditos
* Pol Bastida Gonzalez ⚡
* Linneth Rodrigues 🚀







