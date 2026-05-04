# Unidos por Los Animales - Semana 8

## Proyecto

Aplicacion web Spring Boot separada en backend y frontend.

El backend expone APIs REST con Spring Web, Spring Security, Spring Data JPA, MySQL, H2 para pruebas y JWT.

El frontend usa Spring Boot, Spring Security, Thymeleaf y consume el backend con RestTemplate.

## Usuarios

admin / admin123 / ROLE_ADMIN

gestor / gestor123 / ROLE_GESTOR

visitante / visitante123 / ROLE_VISITANTE

## Rutas principales

POST /api/auth/login es publica y retorna token JWT.

GET /api/public/animales es publica.

GET /api/private/animales es privada.

POST /api/private/animales es privada para ADMIN y GESTOR.

PUT /api/private/animales/{id} es privada para ADMIN y GESTOR.

DELETE /api/private/animales/{id} es privada para ADMIN.

## Base de datos

En ejecucion normal el backend usa MySQL con la conexion definida en backend/src/main/resources/application.properties.

En pruebas el backend usa H2 en memoria con backend/src/test/resources/application-test.properties.

Los tests no dependen de Docker ni de MySQL real.

## Herramientas de cobertura revisadas

JaCoCo

Tipo: software libre.

Mantiene: Eclipse Foundation.

Caracteristicas: cobertura de lineas, ramas, metodos y clases para Java; integracion directa con Maven y Gradle; reportes HTML y XML.

URL: https://www.jacoco.org/jacoco/

Cobertura

Tipo: software libre.

Mantiene: comunidad del proyecto Cobertura.

Caracteristicas: cobertura para Java con reportes HTML y XML; proyecto historico, menos activo para versiones modernas de Java.

URL: https://cobertura.github.io/cobertura/

SonarQube Community Build

Tipo: software libre.

Mantiene: SonarSource.

Caracteristicas: analisis de calidad, seguridad y cobertura; puede importar reportes JaCoCo XML.

URL: https://www.sonarsource.com/products/sonarqube/

## Herramienta seleccionada

Se selecciono JaCoCo porque se integra directamente con Maven, genera reportes HTML y XML, no requiere servidor externo y mide cobertura de backend y frontend dentro del mismo ciclo de build.

## Ejecutar pruebas y cobertura

Backend:

cd backend

.\mvnw.cmd clean test

.\mvnw.cmd clean verify

Frontend:

cd frontend

.\mvnw.cmd clean test

.\mvnw.cmd clean verify

## Reportes JaCoCo

Backend HTML: backend/target/site/jacoco/index.html

Backend XML: backend/target/site/jacoco/jacoco.xml

Frontend HTML: frontend/target/site/jacoco/index.html

Frontend XML: frontend/target/site/jacoco/jacoco.xml

## Cobertura obtenida

Backend: 96.88% de cobertura de lineas.

Frontend: 93.80% de cobertura de lineas.

## Paquetes con menor cobertura

Backend: cl/duocuc/backend/entity con 88.89%.

Frontend: cl/duocuc/frontend/controller con 85.45%.
