package org.example;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSender {

    private static final int SERVER_PORT = 1337;
    private static final int CLIENT_PORT = 1338;
    private static final int TIMEOUT = 10000;

    public static void sendSocket() throws IOException{
        Status state = Status.INIT_STAT;
        int dataPointer = 0;
        boolean finished = false;
        String requestString = "TheQuickBrownRabbit";

        try (var socket = new DatagramSocket(CLIENT_PORT)) {
            socket.setSoTimeout(TIMEOUT);

            final var receivePacket = new DatagramPacket(new byte[2], 2);
            final var sendPacket = new DatagramPacket(new byte[2], 2, InetAddress.getByName("localhost"), SERVER_PORT);

            while (!finished) {
                try {
                    if (state == Status.INIT_STAT) {
                        sendNext(socket, sendPacket, requestString.charAt(dataPointer++), Status.WAIT_FOR_0.controlByte);
                        state = Status.WAIT_FOR_0;
                    }

                    socket.receive(receivePacket);
                    byte receivedData = receivePacket.getData()[0];
                    byte receivedControl = receivePacket.getData()[1];

                    if (state == Status.WAIT_FOR_0 && receivedControl == Status.WAIT_FOR_0.controlByte) {
                        IO.println((char) receivedData + "|" +  (receivedControl == Status.WAIT_FOR_0.controlByte ? "0" : "1"));

                        if(dataPointer < requestString.length()) {
                            sendNext(socket, sendPacket, requestString.charAt(dataPointer++), Status.WAIT_FOR_1.controlByte);
                            state = Status.WAIT_FOR_1;
                        }
                        else{
                            finished = true;
                        }
                    } else if (state == Status.WAIT_FOR_1 && receivedControl == Status.WAIT_FOR_1.controlByte) {
                        IO.println((char) receivedData + "|" + (receivedControl == Status.WAIT_FOR_0.controlByte ? "0" : "1"));
                        if(dataPointer < requestString.length()) {
                            sendNext(socket, sendPacket, requestString.charAt(dataPointer++), Status.WAIT_FOR_0.controlByte);
                            state = Status.WAIT_FOR_0;
                        }
                        else{
                            finished = true;
                        }
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
