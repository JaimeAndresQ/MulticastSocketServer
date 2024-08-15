package jaime.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Session {
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private Socket socket;
    private String clientId;

    //Constructor para instanciar la session
    public Session(Socket socket) {
        try {
            //Asocio la session a un socketCliente
            this.socket = socket;

            //Instancio los objetos serializables que van a permitir la comunicacion de mensajes con el cliente.
            //El objeto serializable asociado al flujo de salida, se deja por default que sea auntoFlush, para que se envien inmediatamente los mensajes.
            this.outputStream = new PrintWriter(this.socket.getOutputStream(), true);
            this.inputStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            //Defino que la ip y el puerto del cliente sera el identificador para cada usuario conectado en el chat.
            this.clientId = (socket.getLocalAddress().getHostAddress());

        } catch (IOException e) {
            e.printStackTrace();
            this.outputStream = null;
            this.inputStream = null;
            this.socket = null;
        }
    }

    //Metodo Getter: para obtener el id del cliente.
    public String getClientId() {
        return clientId;
    }

    //Metodo que permite la lectura de los mensajes del cliente teniendo en cuenta el objeto serializable utilizado para ello.
    public String read() {
        try {
            return this.inputStream.readLine();
        } catch (SocketException e) {
            //Si existe una "SocketException" es porque quizas el cliente cerro sesion o la session se reestablecio
            //Entonces, manejamos la desconexion del cliente.
            System.err.println("Conexión perdida con el cliente: " + this.clientId);
            this.close();
            return null;
        } catch (IOException e) {
            System.err.println("Error de E/S al leer del cliente " + this.clientId);
            e.printStackTrace();
            return null;
        }
    }

    //Metodo que permite el envio de datos desde el servidor al cliente a traves del objeto serializado.
    public boolean write(String data) {
            try {
                //Verificar que el flujo de salida este asociado al flujo de salida del cliente. Instanciado al crear la session con el socket cliente.
                if (outputStream == null) {
                    System.err.println("Error: El flujo de salida está cerrado o no es válido.");
                    return false;
                }
                //Envia la informacion. Si el envio de la informacion es exitoso retorna un true
                outputStream.println(data);
                return true;
        } catch (Exception e) {
                System.err.println("Error inesperado al enviar datos al cliente: " + clientId);
                e.printStackTrace();
                return false;
            }
    }

    //Metodo que se encarga de cerrar todos los recursos asociados con una conexion (Los objetos serializables de salida y entrada, y el Socket Cliente).
    public boolean close() {
        boolean success = true;

        // Intentar cerrar el flujo de salida
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar el flujo de salida para el cliente: " + clientId);
                e.printStackTrace();
                success = false;
            } finally {
                // Limpiar referencia al objeto.
                this.outputStream = null;
            }
        }

        // Intentar cerrar el flujo de entrada
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el flujo de entrada para el cliente: " + clientId);
                e.printStackTrace();
                success = false;
            } finally {
                // Limpiar referencia al objeto.
                this.inputStream = null;
            }
        }

        // Intentar cerrar el socket asociado al cliente.
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket para el cliente: " + clientId);
                e.printStackTrace();
                success = false;
            } finally {
                // Limpiar referencia al socket del cliente.
                this.socket = null;
            }
        }

        return success;
    }

}
