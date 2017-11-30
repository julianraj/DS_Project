package app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Sequencer {

    private static int sequenceNumber = 1;

    public static void main(String[] args) {
        System.out.println("listening to Front-end...");
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            socket.bind(new InetSocketAddress(InetAddress.getByName(Util.SEQUENCER_HOST), Util.SEQUENCER_PORT));

            while (true) {
                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                //receive request with sequence number from sequencer
                socket.receive(packet);
                String request = new String(packet.getData()).replace("\0", "");
                String campus = request.split("-=")[1].substring(0, 3);
                byte[] forwardMessage = (String.format("%04d", sequenceNumber) + "-=" + request).getBytes();
                System.out.println("request: " + new String(forwardMessage));
                sequenceNumber += 1;
                for (int i = 0; i < Util.REPLICA_MANAGER_HOSTS.length; i+=5) {
                    DatagramPacket forward = new DatagramPacket(forwardMessage, forwardMessage.length,
                            InetAddress.getByName(Util.REPLICA_MANAGER_HOSTS[i]), Util.getCampusPort(campus, i));
                    socket.send(forward);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
            socket = null;
        }

        System.out.println("Sequencer stopped...");
    }
}
