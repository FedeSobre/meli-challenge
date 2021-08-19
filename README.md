# MercadoLibre Challenge Técnico

Este repositorio contiene el challenge técnico de MercadoLibre, creado por Federico Santos Sobre.

# Descripción de la aplicación

La aplicación funciona como un navegador de productos de MercadoLibre:

La pantalla principal posee una barra de búsqueda la cual al presionarla dirige a la pantalla de búsqueda, una imagen la cual permite acceder a los productos marcados como favoritos, y una lista de las categorías de productos de MercadoLibre, los cuales al ser seleccionados dirigen a una búsqueda de los productos mas relevantes de la categoría seleccionada.

La pantalla de búsqueda sirve para buscar productos en base a palabras clave (query). Esta posee una lista de los productos buscados recientemente, los cuales pueden ser ingresados en la barra de búsqueda (utilizando la flecha de inserción), o simplemente dirigir a la búsqueda de dicha query seleccionando el elemento.

La pantalla de resultados muestra la lista de productos encontrados en base a los parámetros de búsqueda (ya sea una query o los productos mas relevantes de una categoría). Al igual que la pantalla principal, posee la barra de búsqueda y la imagen de favoritos. Cada producto listado cuenta con el thumbnail, el titulo, el precio y un botón para marcar el producto como favorito. También se puede seleccionar el producto, lo cual lleva a la pantalla de descripción de dicho producto. Al llegar al final de la lista, aparecerá un icono de carga y, si todavía quedan productos por cargar, se cargara la siguiente pagina de productos.

La pantalla de favoritos muestra la lista de los productos que han sido marcados como favoritos. La lista de productos funciona igual a la de la pantalla de resultados. Esta pantalla tiene una imagen que permite acceder a la pantalla de búsqueda.

La pantalla de detalles muestra los detalles de un producto. Los elementos de esta pantalla incluyen la condición del producto (Nuevo/Usado), las unidades vendidas (en algunos casos aproximada), el titulo del producto, la lista de imágenes del producto, un botón para compartir el enlace al producto, el precio del producto, las unidades disponibles (en algunos casos aproximada), la descripción del producto y un botón que permite abrir el producto en la web oficial de MercadoLibre (o en la app si esta instalada).

# Detalles técnicos

* La aplicación tiene compatibilidad a partir de la API 21 de Android (Android 5.0 - Lollipop)
* La aplicación fue desatollada en su totalidad en Kotlin
* La aplicación fue diseñada con el Toolchain de la API 30 de Android (Android 11)
* La versión de Android Studio utilizada es la 4.2.1

# Estilo visual

La aplicación intenta imitar lo mas fielmente posible el estilo de MercadoLibre, utilizando mismos colores, misma estructura general, mismas proporciones, mismos elementos, etc.

# Librerías utilizadas

* Las librerías estándar (Kotlin, AppCompat y Material)
* Las librerías estándar de prueba (JUnit y Espresso)
* La librería de Co-rutinas de Kotlin, utilizada para manejar las tareas asíncronas y concurrentes
* La librería OkHTTP3, utilizada para realizar solicitudes REST a los endpoints de la API de MercadoLibre
* La librería Picasso, utilizada para manejar la carga de imágenes en las views

Como fundamento de la elección de las librerías no estándar, la librería de co-rutinas hace el trabajo en paralelo mucho mas simple que usar Runnables, Threads y enviar eventos al Looper del hilo principal, la librería OkHTTP es ampliamente utilizada y recomendada por la comunidad de desarrolladores de Android, manejando cada aspecto de la comunicación con el servidor remoto, y la librería Picasso (de los mismos creadores de OkHTTP), simplifica la descarga y el caché de imágenes remotas, así como el redimensionamiento de las mismas, evitando tener que descargar la imagen de forma manual y tener que decodificar la imagen usando BitmapFactory (o algún procedimiento similar) para cargar las imágenes con las dimensiones optimas.

# Endpoints utilizados

* El sitio utilizado por la aplicación es MLA (MercadoLibre Argentina)
* **/sites/*$site*/categories**: Utilizado para obtener las categorías de MercadoLibre, donde *$site* es el sitio (MLA, lo cual es igual para los demás endpoints)
* **/sites/*$site*/search?category=*$category*&offset=$offset&limit=50**: Utilizado para obtener los productos mas relevantes de una categoría, donde *$category* es el ID de la categoría y *$offset* el offset utilizado para la paginación
* **/sites/*$site*/search?q=*$query*&offset=*$offset*&limit=50**: Utilizado para obtener los productos que corresponden a una query, donde *$query* es la palabra clave y *$offset* el offset utilizado para la paginación
* **/items/*$item***: Utilizado para obtener los detalles de un producto en particular (en concreto, es utilizado para obtener la lista de imágenes de un producto), donde *$item* es el ID del producto
* **/items?ids=*$ids***: Utilizado para obtener los detalles de varios productos en particular, donde *$ids* es la lista IDs de productos
* **/items/*$item*/description**: Utilizado para obtener la descripción de un producto, donde *$item* es el ID del producto
* **/currencies/*$currency***: Utilizado para obtener el símbolo de una divisa, donde *$currency* es el ID de la divisa

# Testeo

* La aplicación fue probada manualmente en las APIs 21, 23, 24, 28 y 30
* Fueron creadas pruebas unitarias para los 3 componentes lógicos principales de la aplicación (La comunicación con la API de MercadoLibre, el sistema de favoritos y el sistema de divisas) con una cobertura del 100% en cada caso.
* Fue creada una prueba automatizada de la aplicación (utilizando Espresso), la cual recorre cada categoría, entra en múltiples productos, los marca y desmarca como favoritos, y realiza búsquedas basadas en query.

Todas las pruebas (unitarias y automatizada) fueron ejecutadas efectivamente sin ningún fallo en las APIs mencionadas anteriormente.

# Notas:

* El código esta completamente comentado (quitando la parte de las pruebas e instrumentalización)

* El manejo de errores desde el punto de vista del desarrollador es simplemente un registro detallado del error en el Log

* Si ocurre un error que afecta la experiencia del usuario, este se vera reflejado en forma de Toast en la pantalla.

* Con respecto al rendimiento y los memory leaks, la prueba automatizada de la aplicación abre aproximadamente 3.200 productos (guardando cada uno como favorito) y muestra aproximadamente 16.000 productos en la lista de resultados (de a grupos de 250 aprox.) sin generar ningún tipo de inconveniente. Dicha prueba también fue ejecutada con el profiler, el cual arrojo un consumo de energía siempre liviano, memoria por debajo de los 200MB y uso de la CPU de menos del 15% en promedio. Asimismo, esta prueba tomo aproximadamente una hora para completarse.

* Con respecto a los permisos de la aplicación, solo se requiere el permiso a Internet.
