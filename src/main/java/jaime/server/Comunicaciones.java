package jaime.server;

import jaime.java_server_socket.JavaServerSocket;

import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Comunicaciones {
    private final SocketProcess serverListen;
    private final SocketProcess serverBroadcast;
    private final BlockingQueue<String> messageQueue;

    public Comunicaciones(int listenPort, int broadcastPort, int backlog) {
        // Crear y verificar los sockets de escucha y difusión
        ServerSocket listenSocket = createServerSocket(listenPort, backlog);
        ServerSocket broadcastSocket = createServerSocket(broadcastPort, backlog);

        if (listenSocket == null || broadcastSocket == null) {
            throw new RuntimeException("Error creating ServerSockets. Exiting...");
        }

        System.out.println("Server Sockets were created");

        // Crear una cola bloqueante para mensajes
        this.messageQueue = new LinkedBlockingQueue<>();

        // Instanciar los servidores
        this.serverListen = new Server(listenSocket, messageQueue);
        this.serverBroadcast = new Server(broadcastSocket, messageQueue);

        // Iniciar los servidores
        startServerInThread(serverListen, "Listen Server");
        startServerInThread(serverBroadcast, "Broadcast Server");

        // Iniciar la difusión de mensajes
        startBroadcastThread();
    }

    private ServerSocket createServerSocket(int port, int backlog) {
        JavaServerSocket javaServerSocket = new JavaServerSocket(port, backlog);
        ServerSocket serverSocket = javaServerSocket.get();

        if (serverSocket == null) {
            System.err.println("Failed to create ServerSocket on port " + port);
        }

        return serverSocket;
    }

    private void startServerInThread(SocketProcess server, String serverName) {
        new Thread(() -> {
            while (true) {
                try {
                    if (!server.bind()) {
                        System.err.println(serverName + " bind failed");
                    }
                    System.out.println(serverName + " is waiting for new connections...");
                } catch (Exception e) {
                    System.err.println("Error in " + serverName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startBroadcastThread() {
        new Thread(() -> {
            try {
                serverBroadcast.startBroadcast();
            } catch (Exception e) {
                System.err.println("Error in Broadcast Thread: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}

