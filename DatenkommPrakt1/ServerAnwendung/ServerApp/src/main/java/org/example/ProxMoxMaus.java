package org.example;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ProxMoxMaus {

    private static final int SERVER_PORT = 1337;
    private static final int CLIENT_PORT = 1338;
    private static final int PROXY_FOR_SERVER_PORT = 3887;
    private static final int PROXY_FOR_CLIENT_PORT = 3888;

    public static void startProxy() throws Exception {
        DatagramSocket socketFromClient = new DatagramSocket(PROXY_FOR_SERVER_PORT);
        DatagramSocket socketFromServer = new DatagramSocket(PROXY_FOR_CLIENT_PORT);

        InetAddress localhost = InetAddress.getByName("localhost");
        Thread.startVirtualThread(() -> {
            weiterschicken(socketFromClient, localhost, SERVER_PORT, "Client -> Server", true);
        });

        var blub = Thread.startVirtualThread(() -> {
            weiterschicken(socketFromServer, localhost, CLIENT_PORT, "Server -> Client", true);
        });
        blub.join();
        //System.in.read();
    }

    private static void weiterschicken(DatagramSocket listenSocket, InetAddress targetAddr, int targetPort, String direction, boolean dropAllowed) {
        try {

            while (true) {
                byte[] buffer = new byte[2];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                listenSocket.receive(packet);

                char character = (char) buffer[0];
                String bit = (buffer[1] == (byte)0x00) ? "0" : "1";

                if (dropAllowed && Math.random() < 0.20) {
                    IO.println("PROXY SAGT NEIN: " + direction + " -> [" + character + " | Bit " + bit + "]");
                    continue;
                }

                IO.println(direction + ": '" + (char)buffer[0] + "' mit Byte " + String.format("%02X", buffer[1]));

                DatagramPacket forwardPacket = new DatagramPacket(
                        packet.getData(), packet.getLength(), targetAddr, targetPort
                );
                listenSocket.send(forwardPacket);
            }
        } catch (IOException e) {
            System.err.println("Fehler im Proxy (" + direction + "): " + e.getMessage());
        }
    }
}

