package app;

import app.server.ServerPOA;
import org.omg.CORBA.ORB;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class FrontEndImpl extends ServerPOA {

    private ORB mOrb;

    volatile String result = null;

    public void setOrb(ORB orb) {
        this.mOrb = orb;
    }

    public FrontEndImpl() {
    }

    @Override
    public String createRoom(String adminID, int roomNumber, String date, String timeSlots) {
        String message = "create-=" + adminID + "-=" + roomNumber + "-=" + date + "-=" + timeSlots;
        return processMessage(message);
    }

    @Override
    public String deleteRoom(String adminID, int roomNumber, String date, String timeSlots) {
        String message = "delete-=" + adminID + "-=" + roomNumber + "-=" + date + "-=" + timeSlots;
        return processMessage(message);
    }

    @Override
    public String bookRoom(String studentID, String campusName, int roomNumber, String date, String timeSlot) {
        String message = "book-=" + studentID + "-=" + campusName + "-=" + roomNumber + "-=" + date + "-=" + timeSlot;
        return processMessage(message);
    }

    @Override
    public String getAvailableTimeSlots(String studentID, String date) {
        String message = "availability-=" + studentID + "-=" + date;
        String response = processMessage(message);
        return response;
//        return processMessage(message);
    }

    @Override
    public String cancelBooking(String studentID, String bookingID) {
        String message = "cancel-=" + studentID + "-=" + bookingID;
        return processMessage(message);
    }

    @Override
    public String changeReservation(String studentID, String bookingID, String campusName, int roomNumber, String date, String timeSlot) {
        String message = "change-=" + studentID + "-=" + bookingID + "-=" + campusName + "-=" + roomNumber + "-=" + date + "-=" + timeSlot;
        return processMessage(message);
    }

    private String processMessage(String message) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            socket.bind(new InetSocketAddress(InetAddress.getByName(Util.FRONT_END_HOST), Util.FRONT_END_PORT));

            byte[] data = message.getBytes();
            DatagramPacket request = new DatagramPacket(data, data.length, InetAddress.getByName(Util.SEQUENCER_HOST), Util.SEQUENCER_PORT);
            socket.send(request);

            new HandlerThread(socket).start();
            while (result == null) {
            }
            String myResult = result;
            result = null;
            return myResult;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) socket.close();
        }
        return "failed";
    }

    private class HandlerThread extends Thread {

        final DatagramSocket fSocket;

        public HandlerThread(DatagramSocket fSocket) {
            this.fSocket = fSocket;
        }

        @Override
        public void run() {
            List<String> results = new ArrayList<>();
            try {
                //==== remove this ======
                byte[] buffer = new byte[2048];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                fSocket.receive(reply);
                String response = new String(reply.getData()).replace("\0", "");
                result = response;

                DatagramSocket errorSocket = new DatagramSocket();
                byte[] errMessage = ("error").getBytes();
                errorSocket.send(new DatagramPacket(errMessage, errMessage.length, reply.getAddress(), Util.REPLICA_MANAGER_PORT));
                errorSocket.close();
                //=======================
                /*for (int i = 0; i < 4; i++) {
                    byte[] buffer = new byte[2048];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    fSocket.receive(reply);
                    String response = new String(reply.getData()).replace("\0", "");
                    if (!results.contains(response)) {
                        if (results.size() > 0) {
                            //hasError = true;
                            DatagramSocket errorSocket = new DatagramSocket();
                            byte[] errMessage = ("error:" + result).getBytes();
                            errorSocket.send(new DatagramPacket(errMessage, errMessage.length, reply.getAddress(), Util.REPLICA_MANAGER_PORT));
                            errorSocket.close();
                        }
                        results.add(response);
                    } else {
                        result = response;
                        fSocket.setSoTimeout(1000);
                    }
                }*/
            } catch (SocketTimeoutException e) {
                //server down

            } catch (IOException e) {
                e.printStackTrace();
            }

            fSocket.close();
        }
    }
}
