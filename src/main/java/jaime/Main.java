package jaime;

import jaime.java_server_socket.JavaServerSocket;
import jaime.server.Server;
import jaime.server.SocketProcess;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Java Server Socket");

        // Creamos la instancia del server Socket utilizando la librería de java.net
        JavaServerSocket javaServerSocket = new JavaServerSocket(1802, 100);
        ServerSocket serverSocket = javaServerSocket.get();

        // Si el server no se instanció entonces imprimimos un mensaje
        if (serverSocket == null) {
            System.out.println("ServerSocket is null");
            return;
        }

        // Servidor Socket Instanciado
        System.out.println("Server Socket was created");

        // Instanciamos nuestra clase servidor y le pasamos como parámetro el serverSocket creado.
        SocketProcess server = new Server(serverSocket);

        while (true) {

            // Ponemos a que nuestro servidor esté a la espera de conexiones y pueda aceptar alguna conexión con un cliente.
            if (!server.bind()) {
                System.out.println("Server bind failed");
                return;
            }

            System.out.println();
            System.out.println("Server is listening for connections...");

            try {
                // Escuchar solicitudes del cliente
                List<Object> dataRequest = server.listen();

                // Imprimimos los mensajes que llegaron del cliente
                System.out.println("Información recibida del cliente: " + dataRequest);

                if (!dataRequest.isEmpty()) {
                    // Procesamos la solicitud
                    ArrayList<Object> dataResponse = new ArrayList<>();
                    dataResponse.add("Hello from server your request is: ");
                    dataResponse.addAll(dataRequest);
                    dataResponse.add(0); // Añadimos 0 para indicar que la respuesta ha sido procesada

                    // Imprimimos los mensajes enviados desde el servidor al cliente.
                    System.out.println("Información enviada al cliente: " + dataResponse);

                    // Enviamos los mensajes utilizando el método del servidor al cliente.
                    server.response(dataResponse);

                    // Cerramos la conexión del servidor
                    if (!server.close()) {
                        System.out.println("Server close failed");
                    } else {
                        System.out.println("Java Server Socket closed");
                    }
                } else {
                    // Si no hay 0, mantenemos la conexión abierta y enviamos una respuesta
                    ArrayList<Object> dataResponse = new ArrayList<>();
                    dataResponse.add("Hello from server your request is: ");
                    dataResponse.addAll(dataRequest);

                    // Imprimimos los mensajes enviados desde el servidor al cliente.
                    System.out.println("Información enviada al cliente: " + dataResponse);

                    // Enviamos los mensajes utilizando el método del servidor al cliente.
                    server.response(dataResponse);
                }

            } catch (Exception e) {
                System.out.println("Error processing request: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
