package org.example;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPReceiver {

    private static final int SERVER_PORT = 1337;
    private static final int CLIENT_PORT = 1338;
    private static final int TIMEOUT = 10000;

    public static void receiveSocket() throws IOException {
        Status state = Status.WAIT_FOR_0;
        int dataPointer = 0;
        String answerString = "JumpsOverTheLazyFox";

        try (var socket = new DatagramSocket(SERVER_PORT)) {
            final var receivePacket = new DatagramPacket(new byte[2], 2);
            final var sendPacket = new DatagramPacket(new byte[2], 2, InetAddress.getByName("localhost"), CLIENT_PORT);
            socket.setSoTimeout(TIMEOUT);
            while (true) {
                try {
                    socket.receive(receivePacket);
                    byte receivedData = receivePacket.getData()[0];
                    byte receivedControl = receivePacket.getData()[1];
                    if (receivedControl == Status.WAIT_FOR_0.controlByte) {
                        IO.println((char)receivedData + "|" +  (receivedControl == Status.WAIT_FOR_0.controlByte ? "0" : "1"));
                        if(dataPointer < answerString.length()) {
                            sendNext(socket, sendPacket, answerString.charAt(dataPointer++), Status.WAIT_FOR_0.controlByte);
                        }
                        state = Status.WAIT_FOR_1;
                    } else if (receivedControl == Status.WAIT_FOR_1.controlByte) {
                        IO.println((char)receivedData + "|" +  (receivedControl == Status.WAIT_FOR_0.controlByte ? "0" : "1"));
                        if(dataPointer < answerString.length()) {
                            sendNext(socket, sendPacket, answerString.charAt(dataPointer++), Status.WAIT_FOR_1.controlByte);
                        }
                        state = Status.WAIT_FOR_0;
                    }
                } catch (IOException e) {
                    IO.println("Timeout oder Abbruch");
                }
            }
        }
    }
    private static void sendNext(DatagramSocket socket, DatagramPacket packet, char character, byte control) throws IOException {
        packet.getData()[0] = (byte) character;
        packet.getData()[1] = control;
        socket.send(packet);
    }
}
//ss -lun