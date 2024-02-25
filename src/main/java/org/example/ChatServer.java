package org.example;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    static List<String> mensajesPendientes = new ArrayList<>();
    static ArrayList<String> clientsName = new ArrayList<>();
    static Map<String, Integer> clientsLastCon  = new ConcurrentHashMap<>();
    static int globalNumMsg = 0;
    private static boolean running = true;
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    static ArrayList<ClientHandler> clients = new ArrayList<>();
    static ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
    private static final int PUERTO = 6789;


    public static void main(String[] args) {


        try {
            // Instanciamos un nuevo servidor
            ServerSocket serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado y escuchando en el puerto " + PUERTO);


            while (running) {

                // Si recibimos un shutdown/kill, enviamos un mensaje de que el servidor se va apagar, terminamos el bucle y apagamos.
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("El servidor se ha apagado, desconectando...");
                    running = false;
                    executor.shutdown();
                }));

                try {
                    // A cada cliente nuevo que aceptemos, creamos una instancia, la ejecutamos en nuestro executor y la añadimos a la lista de clientes
                    Socket connectionSocket = serverSocket.accept();
                    ClientHandler newClient = new ClientHandler(connectionSocket);
                    executor.execute(newClient);
                    clients.add(newClient);


                } catch (IOException e){
                    System.err.println(e.getLocalizedMessage());
                }
            }
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        } finally {
            executor.shutdown();
            System.out.println("Servidor de chat detenido.");
        }
    }
}
