package org.example;
import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) throws IOException {
        IO.println("Client meuchelt Windows");

        UDPSender.sendSocket();

    }
}
