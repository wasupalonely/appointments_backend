
# API Citas médicas

## Requisitos para ejecutar el proyecto

- **Java JDK**: Versión 17 (en este caso se usa OpenJDK 17)
- **Docker**: Versión 27.5.1 (build 9f9e405)

## Instrucciones para el desarrollo

### 1. Preparación del entorno de base de datos

Para iniciar la base de datos PostgreSQL en Docker:

```
docker-compose up -d db
```

### 2. (Opcional) Interfaz de administración de base de datos

Si deseas utilizar una interfaz gráfica para administrar la base de datos:

```
docker-compose up -d pgadmin
```

### 3. Ejecución del proyecto

**Opción 1**: Ejecución desde terminal con Maven:

```
./mvnw spring-boot:run
```

**Opción 2**: Ejecución desde IntelliJ IDEA:

Presiona el botón de ejecución en la interfaz de IntelliJ como se muestra a continuación:

![image](https://github.com/user-attachments/assets/9ae6aff5-953b-4210-909a-7cddd3f2fae5)

## Documentación API

La documentación de la API estará disponible en:

```
<url>:<puerto>/api-docs
```

Ejemplo de acceso local:
```
localhost:8080/api-docs
```

## Notas adicionales

- Asegúrate de tener todas las dependencias instaladas antes de ejecutar el proyecto
- Verifica que los puertos necesarios estén disponibles
- Para cualquier problema de ejecución, revisa los logs del contenedor Docker o de la aplicación Spring Boot
- Revisar que no hayan conflictos con los puertos de procesos locales (Por ejemplo: que el puerto de la base de datos no sea usado por otro proyecto, porque de esa manera no se podrá ejecutar la aplicación)
