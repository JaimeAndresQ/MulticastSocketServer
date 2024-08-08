package jaime.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Session {
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Socket socket;

    //Constructor que permite crear la session recibiendo como parametro el socket asociado
    public Session(Socket socket) {
        try {
            this.socket = socket;
            this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            this.objectOutputStream = null;
            this.objectInputStream = null;
            this.socket = null;
        }
    }

    //Metodo para leer los datos - Utilizando el objeto ObjectInputStream que permite deserializarlo.
    public Object read() {
        try {
            return this.objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Metodo para escribir (Enviar mensaje) - Utilizando el objeto ObjetOutputStream que permite serializarlo.
    public boolean write(Object data) {
        try {
            this.objectOutputStream.writeObject(data);
            this.objectOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Metodo para cerrar la conexion -- cierra el socket y elimina el flujo de entrada o salida asociado a estos objetos.
    public boolean close() {
        try {
            this.objectOutputStream.close();
            this.objectInputStream.close();
            this.socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
