package org.example;
import java.io.*;
import java.net.*;

public class TCPLoop {



        private static final int PORT = 1337; // Wähle einen freien Port (z.B. analog zu UDP)

        public static void main(String[] args) {
            System.out.println("Server startet auf Port " + PORT + "...");

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    // Wartet auf eine Verbindung vom Client (z. B. netcat)
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                        System.out.println("Client verbunden: " + clientSocket.getRemoteSocketAddress());

                        // Jeder neue Client startet im INIT-Zustand
                        State currentState = State.INIT;
                        String line;

                        // Lese Befehle, solange der Client sendet
                        while ((line = reader.readLine()) != null) {
                            line = line.trim(); // Entfernt Whitespaces/Newline-Reste
                            System.out.println("Erhalten: " + line);

                            try {
                                // Zustand aktualisieren und Antwort senden
                                currentState = currentState.handleCommand(line, writer);
                                System.out.println("Neuer Zustand: " + currentState);
                            } catch (IllegalArgumentException e) {
                                System.out.println("Falscher Befehl! Schließe Verbindung.");
                                break; // Bricht die innere Schleife ab -> schließt den Socket
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Verbindung mit Client beendet/abgebrochen.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
