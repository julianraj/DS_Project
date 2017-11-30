package app;

import app.server.ServerPOA;
import org.omg.CORBA.ORB;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
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

    private String processMessage(String requestMessage) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
//            socket.bind(new InetSocketAddress(InetAddress.getByName(Util.FRONT_END_HOST), Util.FRONT_END_PORT));
            String message = socket.getLocalPort() + "-=" + requestMessage;
            byte[] data = message.getBytes();
            DatagramPacket request = new DatagramPacket(data, data.length, InetAddress.getByName(Util.SEQUENCER_HOST), Util.SEQUENCER_PORT);
            socket.send(request);

            new HandlerThread(socket).start();
            while (result == null) {
            }
            return result;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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
            List<String> resultList = new ArrayList<>();
            List<String> errorCheckList = new ArrayList<>();
            try {
                //==== remove this ======
//                byte[] buffer = new byte[2048];
//                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
//                fSocket.receive(reply);
//                String response = new String(reply.getData()).replace("\0", "");
//                result = response;
//                System.out.println(result);

//                DatagramSocket errorSocket = new DatagramSocket();
//                byte[] errMessage = ("error").getBytes();
//                errorSocket.send(new DatagramPacket(errMessage, errMessage.length, reply.getAddress(), Util.REPLICA_MANAGER_PORT));
//                errorSocket.close();
                //=======================

                for (int i = 0; i < 4; i++) {
                    byte[] buffer = new byte[2048];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    fSocket.receive(reply);
                    String response = new String(reply.getData()).replace("\0", "");
                    String actualResponse = response.split("-=")[1];
                    if (!resultList.contains(actualResponse)) {
                        /*if (result != null && !actualResponse.equals(result)) {
                            error = true;
                        }*/
                    } else {
                        if (result == null) {
                            result = actualResponse;
                            fSocket.setSoTimeout(2000);
                        } else {
                            /*if (!actualResponse.equals(result)) {
                                error = true;
                            }*/
                        }
                    }
                    resultList.add(actualResponse);
                    errorCheckList.add(response);
                }
                checkErrors(errorCheckList);
            } catch (SocketTimeoutException e) {
                checkErrors(errorCheckList);
                //server down
                sendMessageToRMs("error:not-available");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void checkErrors(List<String> results) {
            System.out.println("results: " + Arrays.deepToString(results.toArray()));
            List<String> errorIndexes = new ArrayList<>();
            for (String str : results) {
                String rm = str.split("-=")[0];
                if (!str.split("-=")[1].equals(result)) {
                    System.out.println("error: " + rm);
                    errorIndexes.add(rm);
                }
            }
            if (!errorIndexes.isEmpty()) {
                String errorMessage = "error:" + String.join(",", errorIndexes);
                sendMessageToRMs(errorMessage);
            }
        }

        private void sendMessageToRMs(String message) {
            try {
                for (int i = 0; i < Util.REPLICA_MANAGER_HOSTS.length; i++) {
                    DatagramSocket errorSocket = new DatagramSocket();
                    byte[] errMessage = message.getBytes();
                    errorSocket.send(new DatagramPacket(errMessage, errMessage.length, InetAddress.getByName(Util.REPLICA_MANAGER_HOSTS[i]), Util.REPLICA_MANAGER_PORT[i]));
                    errorSocket.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
