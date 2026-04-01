package org.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReceiver {

    public static void receiveSocket() throws IOException {

        try(var socket = new DatagramSocket(1338)){
            socket.setSoTimeout(5000);
            final var receivePacket = new DatagramPacket(new byte[2], 2);

            try{
                socket.receive(receivePacket);
                IO.println("Paket erfolgreich erhalten!");
                IO.println("---------------------------");
                IO.println(receivePacket);
            }
            catch(IOException e){
                IO.println("Timeout oder Abbruch");
            }
        }
    }
}
//ss -lun