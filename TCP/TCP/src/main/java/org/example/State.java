package org.example;
import java.io.BufferedWriter;

public enum State {

    INIT,
    SLEFT,
    SRIGHT,
    SFINAL;

    public State handleCommand(String command, BufferedWriter writer) throws Exception {
        switch (this) {
            case INIT:
                if ("Left!".equals(command)) {
                    send(writer, "Went Left.");
                    return SLEFT;
                } else if ("Right!".equals(command)) {
                    send(writer, "Went Right.");
                    return SRIGHT;
                } else {
                    send(writer, "Ne du, wir sind in Init");
                    throw new IllegalArgumentException("Falscher Befehl für Zustand " + this);
                }

            case SLEFT:
                if ("GoOn1!".equals(command)) {
                    send(writer, "WentOn1.");
                    send(writer, "Final State reached.");
                    return SFINAL;
                } else {
                    send(writer, "Ne du, wir sind in SLeft");
                    throw new IllegalArgumentException("Falscher Befehl für Zustand " + this);
                }

            case SRIGHT:
                if ("GoOn2!".equals(command)) {
                    send(writer, "WentOn2.");
                    send(writer, "Ende der Eisenbahn.");
                    return SFINAL;
                } else {
                    send(writer, "Ne du, wir sind in SRight");
                    throw new IllegalArgumentException("Falscher Befehl für Zustand " + this);
                }

            case SFINAL:
                if ("Back!".equals(command)) {
                    send(writer, "Went Back.");
                    return INIT;
                } else if ("OnceMore!".equals(command)) {
                    send(writer, "DidOnceMore.");
                    return SLEFT;
                } else {
                    send(writer, "Ne du, wir sind in Sfinal");
                    throw new IllegalArgumentException("Falscher Befehl für Zustand " + this);
                }
        }
        throw new IllegalArgumentException("Unbekannter Zustand. Buh!");
    }

    private void send(BufferedWriter writer, String message) throws Exception {
        writer.write(message + "\n");
        writer.flush();
    }
}