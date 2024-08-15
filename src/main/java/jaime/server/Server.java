package jaime.server;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Server implements SocketProcess {
    ServerSocket serverSocket;
    BlockingQueue<String> messageQueue;

    //Constructor para instanciar el servidor.
    public Server(ServerSocket serverSocket, BlockingQueue<String> messageQueue) {
        this.serverSocket = serverSocket;

        this.messageQueue = messageQueue;
    }

    //Metodo que permite escuchar y aceptar las peticiones de los clientes.
    @Override
    public boolean bind() {
        try {
            //Cuando existe una conexion con el serverSocket, acepta la conexion y devuelve el socket.
            Socket socket = this.serverSocket.accept();

            //Instancio una session asociada al socket que acaba de conectarse al servidor.
            Session session = new Session(socket);

            //Defino el Id del cliente que es la IP donde esta corriendo el socket cliente asociado a la session instanciada.
            String clientId = session.getClientId();



            // Verificar si es el server de escucha o de difusión
            if (this.serverSocket.getLocalPort() == 1802) {

                // Si el servidor es de escucha, entonces asocio el socket cliente a la HashMap de sessiones del ListenServer.
                SessionManager.addListenSession(session);

                System.out.println("Conexión establecida con cliente de listen: " + clientId );

                //Creo un hilo donde asocio la session correspondiente al socketCliente que va a  enviar mensajes a mi ListenServer.
                //Esta funcion escuchara constantemente los mensajes de ese socketCLiente en especifico.
                new Thread(() -> listen(session)).start();


            } else if (this.serverSocket.getLocalPort() == 1803) {
                // Si el servidor es de difusion de mensajes, entonces asocio el socket cliente a la HashMap de sessiones del BroadcastServer.
                SessionManager.addBroadcastSession(session);

                System.out.println("Conexión establecida con cliente broadcast: " + clientId );

                //Creo un nuevo hilo donde emperaza a correr la funcion que permitira enviar mensajes broadcast a los clientes conectados al BroadcastServer.
                new Thread(this::startBroadcast).start();
            }

            //Imprimimos las IP de los clientes asociados a la session de cada socket conectado a cada servidor.
//            System.out.println("Listen Sessions Bind: " + SessionManager.getAllListenId());
//            System.out.println("Broadcast Sessions Bind: " + SessionManager.getAllBroadcastId());

            return true;

            //Manejo de excepciones
        } catch (SocketException e) {
            System.err.println("Error en la conexión de socket: " + e.getMessage());
            e.printStackTrace();
            return false;

        } catch (IOException e) {
            System.err.println("Error al aceptar la conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void listen(Session session) {
        boolean next = true;
        String data;

        while (next) {
            //Intenta recibir la data del cliente
            data = session.read();
            //Imprime lo que recibe de informacion
            System.out.println("Data received: " + data);

            //Si existe infromacion
            if (data != null) {
                try {
                    // Convierte la información recibida en un objeto JSON para acceder a sus claves.
                    JSONObject json = new JSONObject(data);
                    String usuario = json.getString("usuario");
                    String mensaje = json.getString("mensaje");
                    int etiquetaControl = json.getInt("etiqueta_control");


                    System.out.println("Mensaje del cliente: " + mensaje);

                    // Verifica si la etiqueta de control es igual a 0 (indicando una desconexión).
                    if (etiquetaControl == 0) {
                        next = false;

                        //Obtiene el Id asociado a la session actual.
                        String clientId = session.getClientId();

//                        System.out.println("Sessiones Listen antes de borrrar: "+ SessionManager.getAllListenId());
//                        System.out.println("Sesiones Broadcast antes de borrar" + SessionManager.getAllBroadcastId());

                        //Obtiene las sessiones  de escucha y de broadcast asociadas a la ip de la session actual.
                        Session ListenClientRemove = SessionManager.getListenSession(clientId);
                        Session BroadcastClientRemove = SessionManager.getBroadcastSession(clientId);

                        //Elimina la referencia de la session de en el hashMap donde almaceno la lista de sessiones qeu existen.
                        SessionManager.removeSession(ListenClientRemove);
                        SessionManager.removeSession(BroadcastClientRemove);

//                        System.out.println("Sessiones Listen despues de borrrar: "+ SessionManager.getAllListenId());
//                        System.out.println("Sesiones Broadcast despues de borrar" + SessionManager.getAllBroadcastId());

                        session.close();
                    } else {
                        // Si la etiqueta de control no es 0, se considera un mensaje normal.
                        String fullMessage = usuario + ": " + mensaje;
                        // Añade el mensaje a la cola de mensajes.
                        messageQueue.add(fullMessage);
//                        System.out.println(fullMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Si no se recibió data (es nula), maneja la desconexión del cliente.
                System.out.println("Error al traer la data");
                next =false;

                //Obtiene el Id asociado a la session actual.
                String clientId = session.getClientId();

                //Obtiene las sessiones  de escucha y de broadcast asociadas a la ip de la session actual.
                Session ListenClientRemove = SessionManager.getListenSession(clientId);
                Session BroadcastClientRemove = SessionManager.getBroadcastSession(clientId);

                //Elimina la referencia de la session de en el hashMap donde almaceno la lista de sessiones qeu existen.
                SessionManager.removeSession(ListenClientRemove);
                SessionManager.removeSession(BroadcastClientRemove);

                session.close();
                System.out.println("La conexion con el cliente se cerró o falló");
                break;
            }
        }
    }


    @Override
    public void startBroadcast() {
        //Empezamos el ciclo de envio de mensajes
        while (true) {
            try {
                // Toma el mensaje de la cola de forma bloqueante
                String message = messageQueue.take();

                // Validar que el mensaje no sea nulo o vacío
                if (message != null && !message.isEmpty()) {
                    System.out.println("Broadcasting message: " + message);

                    // Llamar al método de respuesta para enviar el mensaje a todos los clientes asociados a mi ServerBroadcast
                    response(message);
                } else {
                    System.out.println("Mensaje vacío o nulo ignorado.");
                }

            } catch (InterruptedException e) {
                System.err.println("El hilo de broadcast fue interrumpido.");
                // Restaurar el estado de interrupción del hilo
                Thread.currentThread().interrupt();
                break; // Salir del bucle si se interrumpe el hilo
            } catch (Exception e) {
                System.err.println("Error al procesar el mensaje en broadcast: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("El hilo de broadcast se ha detenido.");
    }



    //Metodo que me permite el envio de los mensajes a todos los clientes asociados.
    @Override
    public void response(String message) {
        // Obtener la lista de usuarios conectados al servidor de escucha
        List<String> usuarios = new ArrayList<>();
        for (Session session : SessionManager.getAllListenSessions()) {
            usuarios.add(session.getClientId());
        }

//        System.out.println(usuarios);

        // Dividir el mensaje recibido en usuario y mensaje.
        String[] parts = message.split(": ", 2);
        if (parts.length < 2) {
            System.err.println("Mensaje mal formado: " + message);
            return; // Salir si el mensaje no está en el formato esperado
        }
        String emisor = parts[0];
        String mensaje = parts[1];

        // Crear el JSON de respuesta.
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("usuarios", usuarios);
        jsonResponse.put("emisor", emisor);
        jsonResponse.put("mensaje", mensaje);
        // Etiqueta de control por defecto
        jsonResponse.put("etiqueta_control", 1);

        //Convertir ese Json a String para que pueda ser enviado en el formato que lo espera el cliente.
        String jsonResponseString = jsonResponse.toString();

        // Enviar el JSON a todas las sesiones de difusión
        for (Session session : SessionManager.getAllBroadcastSessions()) {
            try {
                session.write(jsonResponseString);
                System.out.println("Mensaje enviado a la sesión: " + session.getClientId() + " mensaje: " + jsonResponseString);
            } catch (Exception e) {
                System.err.println("Error al enviar el mensaje a la sesión: " + session.getClientId());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean close() {
        try {
            // Cerrar todas las sesiones de escucha
            for (Session session : SessionManager.getAllListenSessions()) {
                session.close();
            }

            // Cerrar todas las sesiones de difusión
            for (Session session : SessionManager.getAllBroadcastSessions()) {
                session.close();
            }

            // Cerrar el ServerSocket
            serverSocket.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
