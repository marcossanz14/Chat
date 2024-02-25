package org.example;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static org.example.ChatServer.*;


public class ClientHandler implements Runnable {
    Socket socket;
    String nombre;
    BufferedReader inFromClient;
    BufferedWriter outToClient;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        // Asignamos la entrada del cliente (teclado) y la salida al cliente (lo que ve en la terminal)
        inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outToClient = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            // Texto de bienvenida al cliente
            outToClient.write("-- Bienvenido al chat, introduce tu nombre: \n");
            outToClient.flush();

            // Obtenemos el nombre del cliente
            nombre = inFromClient.readLine();

            // Si ya existia en los clientes logeados, carga los mensajes enviados mientras él no estabaa
            if (clientsName.contains(nombre)){
                obtenerYEnviarMensajesPendientes();
            } else {
                // Si no existe el nombre del cliente, lo añade a la lista de clientes y le asigna su ultima conexion por defecto a la del ultimo mensaje
                clientsName.add(this.nombre);
                clientsLastCon.put(this.nombre, globalNumMsg);
            }

            // Enviamos un mensaje de que nos hemos unido y sumamos un índice global
            enviarMensajeExceptoEsteCliente(nombre + " has joined the chat!\n");
            ChatServer.globalNumMsg++;

            String clientMessage;
            // Si el cliente envia el mensaje bye, suamamos un índice, enviamos un mensaje de que nos desconectamos, nos eliminamos de los clientes y salimos del try
            while ((clientMessage = inFromClient.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("bye")) {
                    ChatServer.globalNumMsg++;
                    enviarMensajeExceptoEsteCliente(nombre + " has left the chat.\n");
                    clients.remove(this);
                    break;
                } else {
                    // Si el cliente envia un mensaje, lo enviamos a los demas clientes y sumamos el indice global
                    ChatServer.globalNumMsg++;
                    enviarMensajeExceptoEsteCliente("[" + nombre + "]: " + clientMessage + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Este método sirve para enviar los mensajes que introducimos al resto de los clientes excepto a nosotros mismos
    void enviarMensajeExceptoEsteCliente(String mensaje) throws IOException {
        // Añadimos el mensaje a los pendientes globales y actualizamos el ultimo mensaje enviado del cliente
        mensajesPendientes.add(mensaje);
        clientsLastCon.replace(this.nombre, globalNumMsg);

        // Enviamos simultáneamente a cada cliente de la lista el mensaje que queremos enviar
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    BufferedWriter outToClient = new BufferedWriter(new OutputStreamWriter(client.socket.getOutputStream()));
                    outToClient.write(mensaje);
                    outToClient.flush();
                }
            }
        }
    }

    // En este método obtenemos todos los mensajes pendientes a partir de nuestra última conexion
    private void obtenerYEnviarMensajesPendientes() throws IOException {

        // Obtenemos nuestro último indice del mensaje enviado
        int lastMsg = clientsLastCon.get(this.nombre);

        // Recuperamos todos los mensajes a partir de nuestro último mensaje en la lista de mensajes pendientes
        for (int i = lastMsg; i < mensajesPendientes.size(); i++) {
            outToClient.write(mensajesPendientes.get(i));
            outToClient.flush();
        }
    }

}
