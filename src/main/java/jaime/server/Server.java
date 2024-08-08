package jaime.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements SocketProcess {
    ServerSocket serverSocket;
    Session session;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.session = null;
    }

    //Este metodo espera y acepta una conexion entrante por parte de un cliente.
    //Ademas crea la session asociada al socket.
    @Override
    public boolean bind() {
        try {
            Socket socket = this.serverSocket.accept();
            //Cuando acepta una conexión, se crea una session.
            System.out.println("Conexión establecida con cliente: " + socket.getLocalAddress() +  " desde el puerto: " + socket.getPort());
            this.session = new Session(socket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Este método se queda escuchando hasta que el cliente socket finalice su envío de mensajes.
    @Override
    public List<Object> listen() {
        ArrayList<Object> dataList = new ArrayList<>();
        boolean next = true;
        Object data = null;
        int flag = 1;

        while(next){
            data = this.session.read();
            if(data != null){
                try {
                    dataList.add(data);
                    // Si el dato es un entero, lo usamos para controlar la terminación de la comunicación.
                    // En este caso la bandera o etiqueta definida para determinar la finalización de la
                    // comunicación es el 0.
                    try {
                        flag = (int) data;
                    } catch (Exception e) {
                        flag = 1;
                    }
                    // Seguirá iterando si aún no se encuentra el 0 en la data recibida.
                    next = flag != 0;

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        return dataList;
    }

    @Override
    public boolean response(List<Object> data) {
        data.forEach(d -> this.session.write(d));
        return true;
    }

    @Override
    public boolean close() {
        boolean successful = this.session.close();
        this.session = null;
        return successful;
    }
}
