package org.example;
import java.io.*;
import java.net.*;

public class TCPLoop {



        private static final int PORT = 1337;

        static void main() {
            System.out.println("Server startet auf Port " + PORT + "...");

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                        System.out.println("Client verbunden: " + clientSocket.getRemoteSocketAddress());

                        State currentState = State.INIT;

                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("Erhalten: " + line);

                            try {
                                currentState = currentState.handleCommand(line, writer);
                                System.out.println("Neuer Zustand: " + currentState);
                            } catch (IllegalArgumentException e) {
                                System.out.println("Falscher Befehl! Sayonara!");
                                break;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Verbindung mit Client abgeschossen.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Port ist blockiert!");
            }
        }
}
