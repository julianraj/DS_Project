package app.rm.replica;

import app.Util;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Replica<S> {
    public static final String campuses[] = new String[]{"KKL", "DVL", "WST"};

    protected boolean hasError;
    protected boolean dataCopied = false;

    protected Map<String, S> serverMap;

    private DatagramSocket mSocket;

    public Replica(boolean hasError) {
        this.hasError = hasError;
        serverMap = new HashMap<>();

        startListening();
    }

    private void startListening() {
        try {
            mSocket = new DatagramSocket(null);
            mSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), Util.REPLICA_PORT));
            while (true) {
                try {
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    mSocket.receive(packet);

                    String request = packet.getData().toString().replace("\0", "");
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

    protected void requestData() {
        System.out.println("requesting data...");
        for (String hostName : Util.REPLICA_MANAGER_HOSTS) {
            new Thread(() -> {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    final InetAddress host = InetAddress.getByName(hostName);
                    DatagramPacket request = new DatagramPacket("getData".getBytes(), 7, host, Util.REPLICA_PORT);
                    socket.send(request);

                    byte[] buffer = new byte[4096];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    socket.receive(reply);
                    if (!dataCopied) {
                        dataCopied = true;
                        String json = new String(buffer).replace("\0", "");
                        mapJsonToData(json);
                        System.out.println("Data copied from working replica");
                    }
                } catch (IOException e) {
                }
            }).start();
        }
    }

    public void start() {
        start(false);
    }

    protected abstract void start(boolean requestData);

    public abstract void stop();

    public abstract void restart();

    protected abstract void mapJsonToData(String json);

    protected abstract String mapDataToJson();

    public abstract boolean ping(String campus);
}
