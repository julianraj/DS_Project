package app.rm;

import app.Util;
import app.rm.replica.Replica;
import app.rm.replica.julian.ReplicaImplJ;
import app.rm.replica.mudra.ReplicaImplM;
import app.rm.replica.rashmi.ReplicaImplR;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class ReplicaManager {

    public static int errorCount = 0;
    private static Replica replica;
    private static Replica replicaBackup;

    public ReplicaManager() {
    }


    public static void main(String[] args) {
        int replicaNum = Integer.parseInt(args[0]);
        boolean hasError = false, available = true;
        if (args.length > 1) {
            hasError = Boolean.parseBoolean(args[1]);
        }

        switch (replicaNum) {
            case 3:
                replica = new ReplicaImplR(replicaNum, hasError);
                replicaBackup = new ReplicaImplR(replicaNum, false);
                replica.start();
                break;
            case 2:
                replica = new ReplicaImplJ(replicaNum, hasError);
                replicaBackup = new ReplicaImplJ(replicaNum, false);
                replica.start();
                break;
            case 1:
                replica = new ReplicaImplR(replicaNum, hasError);
                replicaBackup = new ReplicaImplR(replicaNum, false);
                replica.start();
                break;
            case 0:
                replica = new ReplicaImplM(replicaNum, hasError);
                replicaBackup = new ReplicaImplM(replicaNum, false);
                replica.start();
                break;
            default:
                throw new IllegalStateException("Not a valid argument");
        }

        //start listening to front-end
        System.out.println("listening to Front-end...");
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            socket.bind(new InetSocketAddress(InetAddress.getByName(Util.REPLICA_HOSTS[replicaNum]), Util.REPLICA_MANAGER_PORT[replicaNum]));

            while (true) {
                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                //receive request with sequence number from sequencer
                socket.receive(packet);
                String request = new String(packet.getData()).replace("\0", "");
                if (request.startsWith("error")) {
                    if (request.endsWith("not-available")) {
                        //handleNoResponseFailure();
                    } else {
                        //handle byzantine failure
                        String correctResponse = request.split(":")[1];
                        List<String> data = Arrays.asList(correctResponse.split(","));
                        if (data.contains(String.valueOf(replicaNum))) {
                            errorCount++;
                            if (errorCount > 0) {
                                errorCount = 0;
                                System.out.println("restart replica");
                                replica.restart();
                            }
                        }
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

        System.out.println("RM stopped...");
    }

    private static void handleNoResponseFailure() throws IOException {
        pingServer("KKL");
        pingServer("DVL");
        pingServer("WST");
    }

    private static void pingServer(String campus) throws IOException {
        if (replica != null) {
            new Thread(() -> {
                boolean available = replica.ping(campus);
                if (!available && replica != null) {
                    replica.stop();
                    replica = null;
                    replicaBackup.restart();
                }
            }).start();
        }
    }
}
