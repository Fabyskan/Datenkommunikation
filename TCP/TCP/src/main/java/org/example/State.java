package org.example;
import java.io.BufferedWriter;

public enum State {

    INIT, SLEFT, SRIGHT, SFINAL;

    // Diese Methode prüft den Befehl und gibt den nächsten Zustand zurück.
    // Wenn der Befehl ungültig ist, werfen wir eine Exception (führt zum Verbindungsabbruch).
    public State handleCommand(String command, BufferedWriter writer) throws Exception {
        switch (this) {
            case INIT:
                if ("Left!".equals(command)) {
                    send(writer, "Went Left.");
                    return SLEFT;
                } else if ("Right!".equals(command)) {
                    send(writer, "Went Right.");
                    return SRIGHT;
                }
                break;
            case SLEFT:
                if ("OnceMore!".equals(command)) {
                    send(writer, "DidOnceMore.");
                    return SLEFT;
                } else if ("GoOn1!".equals(command)) {
                    send(writer, "WentOn1.");
                    return SFINAL;
                }
                break;
            case SRIGHT:
                if ("GoOn2!".equals(command)) {
                    send(writer, "WentOn2.");
                    return SFINAL;
                }
                break;
            case SFINAL:
                if ("Back!".equals(command)) {
                    send(writer, "Went Back.");
                    return INIT;
                }
                break;
        }
        // Befehl passt nicht zum aktuellen Zustand!
        throw new IllegalArgumentException("Ungültiger Befehl für Zustand " + this);
    }

    private void send(BufferedWriter writer, String message) throws Exception {
        writer.write(message + "\n"); // Wichtig: laut PDF mit '\n' trennen!
        writer.flush();
    }
}
