package app;

import app.server.ServerPOA;
import org.omg.CORBA.ORB;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FrontEndImpl extends ServerPOA {

    private ORB mOrb;

    public void setOrb(ORB orb) {
        this.mOrb = orb;
    }

    public FrontEndImpl() {
    }

    @Override
    public String createRoom(String adminID, int roomNumber, String date, String[] timeSlots) {
        String campus = adminID.substring(0, 3);
        return null;
    }

    @Override
    public String deleteRoom(String adminID, int roomNumber, String date, String[] timeSlots) {
        String campus = adminID.substring(0, 3);
        return null;
    }

    @Override
    public String bookRoom(String studentID, String campusName, int roomNumber, String date, String timeSlot) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            String message = "book-=" + studentID + "-=" + campusName + "-=" + roomNumber + "-=" + date + "-=" + timeSlot;
            System.out.println("message: " + message);

            byte[] data = message.getBytes();
            DatagramPacket request = new DatagramPacket(data, data.length, InetAddress.getByName(Util.SEQUENCER_HOST), Util.SEQUENCER_PORT);
            socket.send(request);

            byte[] buffer = new byte[2048];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);
            System.out.println("reply received");
            return new String(reply.getData()).replace("\0", "");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (socket != null) socket.close();
        }
        return "";
    }

    @Override
    public String getAvailableTimeSlots(String studentID, String date) {
        String campus = studentID.substring(0, 3);

        return null;
    }

    @Override
    public String cancelBooking(String studentID, String bookingID) {
        String campus = studentID.substring(0, 3);

        return null;
    }

    @Override
    public String changeReservation(String studentID, String bookingID, String campusName, int roomNumber, String date, String timesSlot) {
        String campus = studentID.substring(0, 3);

        return null;
    }
}
