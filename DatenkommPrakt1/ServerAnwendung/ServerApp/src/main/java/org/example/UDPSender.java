package org.example;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSender {

    public static final byte CONTROL_0 = 0x00;
    public static final byte CONTROL_1 = (byte) 0xFF;

    public static void sendSocket() throws IOException{
        Status state = Status.INIT_STAT;
        int dataPointer = 0;
        boolean finished = false;
        String requestString = "TheQuickBrownRabbit";

        try (var socket = new DatagramSocket(1338)) {
            while (!finished) {
                socket.setSoTimeout(20000);
                final var receivePacket = new DatagramPacket(new byte[2], 2);
                final var sendPacket = new DatagramPacket(new byte[2], 2, InetAddress.getByName("localhost"), 1337);
                try {
                    if (state == Status.INIT_STAT) {
                        sendPacket.getData()[0] = (byte) requestString.charAt(dataPointer++);
                        sendPacket.getData()[1] = CONTROL_0;
                        socket.send(sendPacket);
                        state = Status.WAIT_FOR_0;
                    }
                    socket.receive(receivePacket);
                    byte receivedData = receivePacket.getData()[0];
                    byte receivedControl = receivePacket.getData()[1];
                        if (state == Status.WAIT_FOR_0 && receivedControl == CONTROL_0) {
                            IO.println((char)receivedData + "|" +  (receivedControl == CONTROL_0 ? "0" : "1"));
                            if(dataPointer < requestString.length()) {
                                sendPacket.getData()[0] = (byte) requestString.charAt(dataPointer++);
                                sendPacket.getData()[1] = CONTROL_1;
                                socket.send(sendPacket);
                                state = Status.WAIT_FOR_1;
                            }
                            else{
                                finished = true;
                            }
                        } else if (state == Status.WAIT_FOR_1 && receivedControl == CONTROL_1) {
                            IO.println((char) receivedData + "|" + (receivedControl == CONTROL_0 ? "0" : "1"));
                            if(dataPointer < requestString.length()) {
                                sendPacket.getData()[0] = (byte) requestString.charAt(dataPointer++);
                                sendPacket.getData()[1] = CONTROL_0;
                                socket.send(sendPacket);
                                state = Status.WAIT_FOR_0;
                            }
                            else{
                                finished = true;
                            }
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
