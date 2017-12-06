package app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sequencer {

    private static int sequenceNumber = 1;
    private static HashMap<Integer, byte[]> queue = new HashMap<>();
    private static HashMap<Integer, HashSet<String>> ack_hashmap = new HashMap<>();
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

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
                if ((request.split("-=")[0]).equals("ack")) {
                    System.out.println(request);
                    ack_hashmap.get(Integer.valueOf(request.split("-=")[2])).add(request.split("-=")[1]);

                } else {
                    byte[] forwardMessage = (String.format("%04d", sequenceNumber) + "-=" + request).getBytes();

                    queue.put(sequenceNumber, forwardMessage);
                    ack_hashmap.put(sequenceNumber, new HashSet<>());
                    startActScheduler(sequenceNumber, 1);

                    System.out.println("request: " + new String(forwardMessage));
                    sequenceNumber += 1;
                    for (int i = 0; i < Util.REPLICA_HOSTS.length; i++) {
                        sendToReplica(i, forwardMessage);
                    }
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

    private static void startActScheduler(int seq, final int attempt) {
        System.out.println("scheduler started for " + seq + "/" + attempt);

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                boolean contains = true;
                for (int i = 0; i < 4; i++) {
                    HashSet<String> acks = ack_hashmap.get(seq);
                    if (!acks.contains(String.valueOf(i))) {
                        System.out.println("resend for replica " + i);
                        sendToReplica(i, queue.get(seq));
                        contains = false;
                    }
                }
                if (contains) {
                    queue.remove(seq);
                    ack_hashmap.remove(seq);
                } else if (attempt < 4) {
                    startActScheduler(seq, attempt + 1);
                }
            }
        }, 2, TimeUnit.SECONDS);
    }

    private static void sendToReplica(int replica, byte[] forwardMessage) {
        try {
            DatagramSocket socket = new DatagramSocket();
            String campus = new String(forwardMessage).split("-=")[3].substring(0, 3);
            DatagramPacket forward = new DatagramPacket(forwardMessage, forwardMessage.length,
                    InetAddress.getByName(Util.REPLICA_HOSTS[replica]), Util.getCampusPort(campus, replica));
            socket.send(forward);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
