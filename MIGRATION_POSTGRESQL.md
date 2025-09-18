# Migración a PostgreSQL - Completada
**Creado por Bernard Orozco**

## Resumen de cambios realizados

### 1. ✅ Configuración de application.properties
- **Archivo**: `src/main/resources/application.properties`
- **Cambios**:
  - Reemplazada configuración de H2 con PostgreSQL
  - URL: `jdbc:postgresql://localhost:5432/portfolio_db`
  - Usuario: `postgres`
  - Password: `ADMIN`
  - Dialect: `PostgreSQLDialect`
  - DDL: Cambiado de `create-drop` a `update` para persistencia

### 2. ✅ Dependencia Maven actualizada
- **Archivo**: `pom.xml`
- PostgreSQL driver ya estaba presente, actualizado scope a `runtime`
- Versión administrada por Spring Boot parent

### 3. ✅ Scripts de base de datos creados
- **create-database.sql**: Script SQL puro para crear la base de datos
- **create-database.bat**: Script batch automatizado para Windows

### 4. ✅ Base de datos creada
- Base de datos `portfolio_db` creada exitosamente en PostgreSQL 17.6
- Lista para recibir conexiones de Spring Boot

## Próximos pasos

1. **Iniciar la aplicación Spring Boot**:
   ```bash
   ./mvnw.cmd spring-boot:run
   ```

2. **Verificar la conexión**:
   - Spring Boot creará automáticamente las tablas en PostgreSQL
   - Los logs mostrarán la conexión exitosa y creación de esquema

3. **Verificar las tablas creadas**:
   ```sql
   -- Conectar con psql
   "C:\Program Files\PostgreSQL\17\bin\psql" -U postgres -d portfolio_db

   -- Ver todas las tablas
   \dt

   -- Ver estructura de una tabla específica
   \d nombre_tabla
   ```

## Notas importantes

- **Persistencia**: Con `ddl-auto=update`, los datos persisten entre reinicios
- **H2 Console**: Ya no está disponible (era específico de H2)
- **Migración de datos**: Si había datos importantes en H2, considerar exportarlos antes
- **Backup**: Configurar backups regulares de PostgreSQL para producción

## Rollback (si necesario)

Para volver a H2, restaurar en `application.properties`:
```properties
# H2 Database Configuration (In-memory for development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

## Configuración exitosa ✓
La migración de H2 a PostgreSQL 17.6 ha sido completada exitosamente.