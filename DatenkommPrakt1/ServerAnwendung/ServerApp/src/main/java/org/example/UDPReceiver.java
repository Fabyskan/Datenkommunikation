package org.example;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPReceiver {

    private static final int SERVER_PORT = 1337;
    private static final int CLIENT_PORT = 3888;
    private static final int TIMEOUT = 50000;

    public static void receiveSocket() throws IOException {
        byte lastProcessedControl = (byte) 0x01;
        //Status state = Status.WAIT_FOR_0;
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

                    if (receivedControl != lastProcessedControl) {
                        String bitDisplay = (receivedControl == Status.WAIT_FOR_0.controlByte) ? "0" : "1";
                        IO.println((char) receivedData + "|" + bitDisplay);

                        if(dataPointer < answerString.length()) {
                            sendNext(socket, sendPacket, answerString.charAt(dataPointer++), receivedControl);
                        } else {
                            sendNext(socket, sendPacket, (char) 0, receivedControl);
                        }
                        lastProcessedControl = receivedControl;


                    } else {

                        IO.println("Duplikat erkannt (Bit " + ((receivedControl == 0) ? "0" : "1") + "). Sende ACK erneut.");

                        if(dataPointer > 0) {
                            sendNext(socket, sendPacket, answerString.charAt(dataPointer - 1), receivedControl);
                        } else {
                            sendNext(socket, sendPacket, (char) 0, receivedControl);
                        }
                    }
                } catch (IOException e) {
                    IO.println("Timeout!");
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
