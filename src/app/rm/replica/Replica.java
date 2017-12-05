package app.rm.replica;

import app.Util;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Replica<S> {
    public static final String campuses[] = new String[]{"KKL", "DVL", "WST"};

    protected boolean hasError;
    protected boolean isAvailable = true;
    protected boolean dataCopied = false;
    protected final int replicaIndex;
    protected Map<String, S> serverMap;

    private DatagramSocket mSocket;

    public Replica(int replicaIndex, boolean hasError) {
        this.replicaIndex = replicaIndex;
        this.hasError = hasError;
        serverMap = new HashMap<>();
    }

    public Replica(int replicaIndex, boolean hasError, boolean isAvailable) {
        this(replicaIndex, hasError);
        this.isAvailable = isAvailable;
    }

    private void startListening() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mSocket = new DatagramSocket(null);
                    mSocket.bind(new InetSocketAddress(InetAddress.getByName(Util.REPLICA_HOSTS[replicaIndex]), Util.REPLICA_PORT[replicaIndex]));
                    while (true) {
                        try {
                            byte[] buffer = new byte[2048];
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            mSocket.receive(packet);

                            String request = new String(packet.getData()).replace("\0", "");
                            System.out.println("replica request:" + request);
                            if (request.contains("getData")) {
                                String response = mapDataToJson();
                                DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), packet.getAddress(), packet.getPort());
                                mSocket.send(reply);
                            }
                        } catch (SocketException e) {
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (mSocket != null)
                        mSocket.close();
                    mSocket = null;
                }
            }
        }.start();
    }

    protected void requestData() {
        System.out.println("requesting data...");
        for (int i = 0; (i < Util.REPLICA_HOSTS.length); i++) {
            final int index = i;
            if (index != replicaIndex) {
                new Thread(() -> {
                    try {
                        DatagramSocket socket = new DatagramSocket();
                        final InetAddress host = InetAddress.getByName(Util.REPLICA_HOSTS[index]);
                        DatagramPacket request = new DatagramPacket("getData".getBytes(), 7, host, Util.REPLICA_PORT[index]);
                        socket.send(request);

                        byte[] buffer = new byte[4096];
                        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                        socket.setSoTimeout(3000);
                        socket.receive(reply);
                        if (!dataCopied) {
                            dataCopied = true;
                            String json = new String(buffer).replace("\0", "");
                            mapJsonToData(json);
                            System.out.println("Data copied from working replica");
                        }
                    }catch (SocketTimeoutException e){

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    public void start() {
        if (isAvailable) {
            startListening();
            start(false);
        }
    }

    protected abstract void start(boolean requestData);

    public abstract void stop();

    public abstract void restart();

    protected abstract void mapJsonToData(String json);

    protected abstract String mapDataToJson();

    public boolean ping(String campus) {
        System.out.println("ping " + campus + " server...");
        try {
            DatagramSocket socket = new DatagramSocket();
            final InetAddress host = InetAddress.getByName(Util.REPLICA_HOSTS[replicaIndex]);
            DatagramPacket request = new DatagramPacket("ping".getBytes(), 4, host, Util.getCampusPort(campus, replicaIndex));
            socket.send(request);

            byte[] buffer = new byte[2048];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(4000);
            try {
                socket.receive(reply);
                return true;
            } catch (SocketTimeoutException e) {
            }
        } catch (IOException e) {
            System.out.println(campus + ": " + e.getMessage());
        }
        return false;
    }
}
