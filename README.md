Similar Products API

Aplicación Spring Boot que expone una API REST para obtener productos similares a partir de un producto determinado.

Requisitos

Java 21

Maven 

Ejecución de la aplicación

El proyecto utiliza una estructura Maven multi-módulo.

Desde la raíz del proyecto, primero compilar e instalar los módulos:

mvn clean install


Después, arrancar la aplicación desde el módulo boot:

mvn spring-boot:run -f boot/pom.xml


La aplicación se ejecuta en el puerto 5000.

La API estará disponible en:

http://localhost:5000

API
Obtener productos similares
GET /product/{productId}/similar


Ejemplo:

curl http://localhost:5000/product/1/similar


Respuesta:

[
  {
    "id": "2",
    "name": "Dress",
    "price": 19.99,
    "availability": true
  },
  {
    "id": "3",
    "name": "Blazer",
    "price": 29.99,
    "availability": false
  }
]

Tests

Para ejecutar los tests automatizados desde la raíz del proyecto:

mvn clean test

Decisiones técnicas

La aplicación utiliza una arquitectura basada en Ports & Adapters, separando la API REST, la lógica de aplicación y las integraciones con servicios externos.

Las APIs externas se consumen mediante WebClient.

La aplicación incluye:

Comunicación HTTP reactiva y no bloqueante.

Timeout de 2 segundos para las llamadas a servicios externos.

Límite de 10 llamadas concurrentes a servicios externos.

Manejo centralizado de errores.

Tests automatizados.

Los errores de los servicios externos se traducen a respuestas HTTP adecuadas:

404 Not Found: cuando el producto no existe.

502 Bad Gateway: cuando se produce un error o timeout al comunicarse con los servicios externos.