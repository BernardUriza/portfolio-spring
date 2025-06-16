
# Portfolio API

Portfolio API es un servicio RESTful desarrollado con **Java 21** y **Spring Boot 3.5.0** que permite gestionar un portafolio de proyectos, habilidades, experiencias laborales y contactos. Este backend está diseñado para ser consumido por interfaces frontend como aplicaciones web, móviles o herramientas de automatización, ofreciendo un conjunto de endpoints seguros, escalables y fáciles de integrar.

---

## 🚀 **Tecnologías utilizadas**
- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Data JPA**
- **Hibernate ORM**
- **H2 (desarrollo)** / **PostgreSQL (producción OnRender)**
- **Jakarta Bean Validation**
- **Maven**
- **JUnit + Mockito para testing**
- **Docker (opcional para despliegues)**

---

## 📦 **Instalación y ejecución local**

1️⃣ Clona el repositorio:
```bash
git clone https://github.com/tu-usuario/portfolio-api.git
cd portfolio-api
````

2️⃣ Ejecuta con Maven:

```bash
./mvnw spring-boot:run
```

El servicio estará disponible en:
👉 [http://localhost:8080](http://localhost:8080)

3️⃣ Accede a la consola H2 (solo desarrollo):
👉 [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
JDBC URL: `jdbc:h2:mem:testdb`

---

## 🌐 **Despliegue en producción**

Actualmente, el proyecto se encuentra desplegado en OnRender:

👉 [https://portfolio-spring-1-jhxz.onrender.com](https://portfolio-spring-1-jhxz.onrender.com)

---

## 🔑 **Endpoints disponibles**

### 📁 **Projects**

| Método   | Endpoint             | Descripción                      |
| -------- | -------------------- | -------------------------------- |
| `GET`    | `/api/projects`      | Listar todos los proyectos       |
| `POST`   | `/api/projects`      | Crear un nuevo proyecto          |
| `PUT`    | `/api/projects/{id}` | Actualizar un proyecto existente |
| `DELETE` | `/api/projects/{id}` | Eliminar un proyecto             |

#### JSON ejemplo:

```json
{
  "title": "Portfolio Website",
  "description": "Showcase of my work",
  "link": "https://my-portfolio.com",
  "createdDate": "2025-06-14"
}
```

---

### 🛠 **Skills**

| Método   | Endpoint           | Descripción                  |
| -------- | ------------------ | ---------------------------- |
| `GET`    | `/api/skills`      | Listar todas las habilidades |
| `POST`   | `/api/skills`      | Crear una nueva habilidad    |
| `PUT`    | `/api/skills/{id}` | Actualizar una habilidad     |
| `DELETE` | `/api/skills/{id}` | Eliminar una habilidad       |

#### JSON ejemplo:

```json
{
  "name": "Spring Boot",
  "description": "Framework for Java microservices"
}
```

---

### 💼 **Experience**

| Método   | Endpoint               | Descripción                   |
| -------- | ---------------------- | ----------------------------- |
| `GET`    | `/api/experience`      | Listar experiencias laborales |
| `POST`   | `/api/experience`      | Crear una nueva experiencia   |
| `PUT`    | `/api/experience/{id}` | Actualizar una experiencia    |
| `DELETE` | `/api/experience/{id}` | Eliminar una experiencia      |

#### JSON ejemplo:

```json
{
  "title": "Backend Developer",
  "company": "TechCorp",
  "description": "Developed microservices and APIs"
}
```

---

### 📧 **Contacts**

| Método   | Endpoint             | Descripción                           |
| -------- | -------------------- | ------------------------------------- |
| `GET`    | `/api/contacts`      | Listar todos los mensajes de contacto |
| `POST`   | `/api/contacts`      | Enviar un mensaje de contacto         |
| `DELETE` | `/api/contacts/{id}` | Eliminar un mensaje                   |

#### JSON ejemplo:

```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "message": "I would like to hire you"
}
```

---

## ⚙ **Arquitectura y flujo lógico**

La aplicación sigue un diseño de **capas**:

* **Controller**: expone los endpoints REST.
* **Service**: contiene la lógica de negocio.
* **Repository**: maneja la persistencia de datos con JPA.
* **Entity / DTO**: modelos de dominio y objetos de transporte para desacoplar la API de la base de datos.

Cada flujo de petición:

1. Llega al Controller y se valida (`@Valid`, `@NotNull`).
2. Se transforma el DTO en Entity (y viceversa al responder).
3. Service aplica la lógica (crear, leer, actualizar, eliminar).
4. Repository realiza la consulta con Hibernate/JPA.
5. Se devuelve un `ResponseEntity` con el estado HTTP adecuado (`200 OK`, `201 Created`, `204 No Content`, `404 Not Found`).

---

## ✅ **Pruebas**

Se incluyen tests unitarios y de integración para los controladores principales usando:

* **JUnit 5**
* **Mockito**
* **Spring Boot Test**

Puedes ejecutarlos con:

```bash
./mvnw clean test
```

---

## 🧪 **Postman / API Clients**

Puedes importar las colecciones desde un archivo JSON o crear peticiones tipo:

```http
POST http://localhost:8080/api/projects
Content-Type: application/json

{
  "title": "New Project",
  "description": "API integration",
  "link": "https://github.com",
  "createdDate": "2025-06-14"
}
```

⚡ *Recomendado:* Agrupa todas las peticiones bajo una colección `Portfolio API` en Postman.

---

## 🔒 **Mejoras futuras**

* Integración de Spring Security con JWT
* Soporte para usuarios y autenticación
* Documentación automática con Swagger/OpenAPI
* Soporte multi-idioma (i18n)
* Despliegue con Docker y CI/CD

---

## 🤝 **Contribuciones**

¡Contribuciones son bienvenidas!
1️⃣ Haz un fork
2️⃣ Crea una rama `feature/nueva-funcionalidad`
3️⃣ Haz commit de tus cambios
4️⃣ Haz un PR

---

## 📄 **Licencia**

Este proyecto se distribuye bajo la licencia MIT.

```

---

✅ Si quieres **que lo genere como archivo descargable (README.md)** o **que lo formatee con badges, tabla de contenidos, o diagrama de arquitectura**, solo dímelo y lo creo al instante 🔨🤖🔧. ¿Te gustaría eso? 🚀
```
