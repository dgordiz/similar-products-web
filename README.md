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


Los productos se devuelven manteniendo el orden proporcionado por el servicio de productos similares.

Si durante la consulta de un producto concreto se produce un error, se continúa procesando el resto de productos y se devuelve la información disponible.

Decisiones técnicas

La aplicación utiliza una arquitectura basada en Ports & Adapters, separando la API REST, la lógica de aplicación y las integraciones con servicios externos.

La estructura está organizada en los siguientes módulos:

application: casos de uso, dominio y puertos.

driving/api-rest: adaptador REST, controladores y mapeadores.

driven/rest-repository: integración con las APIs externas.

boot: configuración y composición de la aplicación.

La capa de aplicación se mantiene independiente de Spring, WebFlux y de los códigos HTTP. La traducción de errores y códigos de estado se realiza en los adaptadores.

Las APIs externas se consumen mediante WebClient.

Comunicación con servicios externos

La aplicación utiliza comunicación HTTP reactiva y no bloqueante para las llamadas al catálogo externo.

Se ha configurado un timeout de 2 segundos para las llamadas a servicios externos.

Los errores de comunicación se traducen a errores de aplicación y posteriormente a las respuestas HTTP correspondientes.

Concurrencia

La recuperación de los productos similares se realiza de forma concurrente para reducir la latencia total.

Se establece un límite de 10 llamadas concurrentes por petición:

MAX_CONCURRENCY = 10


El procesamiento mantiene el orden original de los identificadores recibidos.

Por ejemplo, aunque el producto 3 responda antes que el producto 2, la respuesta mantiene el orden:

2
3
4

Protección global mediante Bulkhead

Además del límite de concurrencia por petición, se ha incorporado un Bulkhead global mediante Resilience4j para limitar el número total de llamadas concurrentes al catálogo.

Configuración:

resilience4j:
  bulkhead:
    instances:
      existing-catalog:
        max-concurrent-calls: 50
        max-wait-duration: 100ms


El límite de 10 llamadas concurrentes se aplica dentro de cada petición, mientras que el Bulkhead limita a 50 las llamadas concurrentes al catálogo considerando todas las peticiones de la aplicación.

Esto evita que un número elevado de peticiones simultáneas pueda provocar una sobrecarga del servicio externo.

El valor del Bulkhead debe dimensionarse teniendo en cuenta la capacidad del catálogo externo, la latencia de las llamadas, el tráfico esperado y los límites de infraestructura.

Manejo de errores

Los errores de los servicios externos se traducen a respuestas HTTP adecuadas:

404 Not Found: cuando el producto no existe.

502 Bad Gateway: cuando se produce un error de comunicación con un servicio externo.

504 Gateway Timeout: cuando se produce un timeout o el servicio externo devuelve un 504.

Los errores se gestionan de forma centralizada en el adaptador REST.

Cuando falla la recuperación de un producto similar concreto, el error no provoca el fallo completo de la petición. El producto afectado se omite y se devuelven los productos que se hayan podido recuperar correctamente.

Observabilidad

La aplicación incluye Spring Boot Actuator para proporcionar información operacional de la aplicación y sus métricas.

También se han añadido logs contextualizados en el adaptador que comunica con el catálogo externo.

Los logs proporcionan información sobre:

producto solicitado;

número de productos similares recuperados;

llamadas realizadas al catálogo;

productos recuperados correctamente;

errores HTTP;

timeouts;

errores de conexión;

productos que no han podido recuperarse.

Ejemplo:

Getting similar products for productId=1
Calling existing catalog for similar product ids, productId=1
Similar product ids retrieved, productId=1, count=3
Calling existing catalog for productId=2
Product retrieved successfully, productId=2


Los errores se registran con el contexto del producto afectado para facilitar el diagnóstico de fallos parciales.

Tests

Para ejecutar los tests automatizados desde la raíz del proyecto:

mvn clean test

Para ejecutar la compilación completa:

mvn clean install

Estructura de la solución
similar-products-web
│
├── application
│   └── dominio, casos de uso y puertos
│
├── driving
│   └── api-rest
│       └── controlador REST y mapeadores
│
├── driven
│   └── rest-repository
│       └── cliente HTTP y adaptadores
│
└── boot
    └── configuración y arranque de Spring Boot

