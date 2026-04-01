package org.example;
import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException {
        IO.println("Server fährt hoch....");

        UDPReceiver.receiveSocket();

    }
}
