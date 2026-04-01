package org.example;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPReceiver {

    public static final byte CONTROL_0 = 0x00;
    public static final byte CONTROL_1 = (byte) 0xFF;

    public static void receiveSocket() throws IOException {
        Status state = Status.WAIT_FOR_0;
        int dataPointer = 0;
        String answerString = "JumpsOverTheLazyFox";

        try (var socket = new DatagramSocket(1337)) {
            while (true) {
                socket.setSoTimeout(20000);
                final var receivePacket = new DatagramPacket(new byte[2], 2);
                final var sendPacket = new DatagramPacket(new byte[2], 2, InetAddress.getByName("localhost"), 1338);
                try {
                    socket.receive(receivePacket);
                    byte receivedData = receivePacket.getData()[0];
                    byte receivedControl = receivePacket.getData()[1];
                    if (state == Status.WAIT_FOR_0 && receivedControl == CONTROL_0) {
                        IO.println((char)receivedData + "|" +  (receivedControl == CONTROL_0 ? "0" : "1"));
                        if(dataPointer < answerString.length()) {
                            sendPacket.getData()[0] = (byte) answerString.charAt(dataPointer++);
                            sendPacket.getData()[1] = CONTROL_0;
                            socket.send(sendPacket);
                        }
                        state = Status.WAIT_FOR_1;
                    } else if (state == Status.WAIT_FOR_1 && receivedControl == CONTROL_1) {
                        IO.println((char)receivedData + "|" +  (receivedControl == CONTROL_0 ? "0" : "1"));
                        if(dataPointer < answerString.length()) {
                            sendPacket.getData()[0] = (byte) answerString.charAt(dataPointer++);
                            sendPacket.getData()[1] = CONTROL_1;
                            socket.send(sendPacket);
                        }
                        state = Status.WAIT_FOR_0;
                    } else {
                        continue;
                    }
                } catch (IOException e) {
                    IO.println("Timeout oder Abbruch");
                }
            }
        }
    }
}
//ss -lun