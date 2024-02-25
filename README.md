# Servidor de Chat Concurrente en Java

En este proyecto, he creado un chat concurrente via localhost y el puerto que le asignemos (6789) por default.

## ChatServer.java

### Atributos

 - List<String> mensajesPendientes: aquí almaceno todos los mensajes de los clientes (incluidos los de conectado, desconectado).
 - ArrayList<String> clientsName: se almacenan los nombres de los clientes que se han logeado en el chat en ejecución (incluso si se han desconectado).
 - Map<String, Integer> clientsLastCon: en este mapa guardo el índice del ultimo mensaje del cliente al desconectarse.
 - int globalNumMsg: este es el índice global de los mensajes, es decir en cada mensaje de cualquier cliente se incrementa en uno.
 - boolean running: almacena si el servidor está en funcionamiento
 - int MAX_THREADS = Runtime.getRuntime().availableProcessors();: ontiene el número de núcleos de CPU disponibles.
 - ArrayList<ClientHandler> clients: esta lista almacena los clientes (solo activos) en el chat.
 - ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);: creamos un pool para manejar las conexiones de clientes en hilos separados.

### Lógica

1. Primero de todo instancio el socket del servidor con el puerto asignado
2. Creamos un bucle (de running) que hará que nuestro servidor esté en bucle
3. Comprobamos con el runtime si hemos sido apagados, en ese caso enviamos un mensaje y nos apagamos
4. Creamos un socket de conexión para cuando el servidor acepte cada nueva petición.

## ClientHandler.java

### Atributos

 - Socket socket: el socket que nos da el servidor al conectarnos
 - String nombre: nombre del cliente
 - BufferedReader inFromClient: un BufferReader para la lectura de lo que introduce el cliente (a partir del socket)
 - BufferedWriter outToClient: para escribirle al cliente en su terminal (a partir del socket)

### Lógica

1. Nada más hacer run, mostramos un mensaje de bienvenida al cliente
2. Obtenemos su nombre en su primera entrada a teclado
3. Comprobamos si el nombre existe
  3.1 Si existe recuparemos los mensajes pendientes
  3.2 Si no existe, le añadimos a la lista de nombres de los clientes y le pasamos de ultima conexion el ultimo indice del mensaje
4. Enviamos un mensaje al resto de clientes de que nos hemos unido al chat y sumamos uno al indice global
5. Creamos un bucle en espera de una entrada a teclado
   5.1 Si el cliente introduce bye, enviamos un mensaje de despedida, sumamos uno al indice global, nos eliminamos de los clientes actuales y nos salimos del bucle.
   5.2 Si no, envía el mensaje al resto de clientes (con su nombre delante) y sumamos uno al indice global

El método de enviarMensajeExceptoEsteCliente recibe un mensaje y lo trata de la siguiente manera:
  1. Añade el mensaje a los mensajes pendientes globales
  2. Reemplaza el ultimo mensaje enviado del cliente al indice actual del mensaje.
  3. Usamos synchronized para que el siguiente código se haga de manera concurrente para cada cliente conectado:
     3.1 Hacemos un bucle de los clientes conectados (expepto el nuestro para que no se nos reenvie a nosotros)
     3.2 Creamos un BufferedWriter para la escritura al socket del cliente iterado
     3.3 Escribimos en este BufferedWriter el mensaje que queremos enviar
     3.4 Hacemos flush para enviarlo y que se muestre

Para el método obtenerYEnviarMensajesPendientes hago:
 1. Obtengo el índice del último mensaje enviado por el cliente actual
 2. Hago un bucle de los mensajes pendientes desde el último mensaje enviado por el cliente actual hasta el último mensaje enviado por todos los clientes (donde el cliente actual estaba desconectado)
    2.1 Escribo al mensaje actual uno a uno cada mensaje pendiente al cliente actual


Marcos Sanz DAM-B
