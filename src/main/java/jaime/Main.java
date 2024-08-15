package jaime;

import jaime.server.Comunicaciones;

public class Main {
    public static void main(String[] args) {
        System.out.println("Java Server Socket");

        Comunicaciones comunicaciones = new Comunicaciones(1802, 1803, 100);
    }
}
