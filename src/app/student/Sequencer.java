package app.student;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sequencer {

    private static int count = 0;

    public static void main(String[] args) throws Exception {
        startUDPSocket();
    }

    private static void startUDPSocket() throws Exception {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(6001);
            while (true) {
                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String request = new String(packet.getData());

                String response = (++count) + "-=" + request;
                DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), InetAddress.getByName("localhost"), 5001);
                socket.send(reply);
            }
        } finally {
            if (socket != null)
                socket.close();
            socket = null;
        }
    }
}
