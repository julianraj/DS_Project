package app.rm.replica.julian;

import app.Util;
import app.server.ServerOperations;

import java.net.*;
import java.util.*;

public class Server implements ServerOperations {

    private HashMap<String, HashMap<Integer, List<RoomRecord>>> mData;
    private HashMap<String, Integer> mStudentData;
    private String mCampusName;
    private static long mBookingId = 0;

    private DatagramSocket mSocket = null;

    private boolean notKilled = true;
    private boolean hasError = false;

    public Server(String campusName, HashMap<String, HashMap<Integer, List<RoomRecord>>> mData, HashMap<String, Integer> mStudentData) {
        this.mCampusName = campusName;
        this.mData = mData;
        this.mStudentData = mStudentData;
    }

    public void start() throws Exception {
        try {
            mSocket = new DatagramSocket(null);
            mSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), Util.getCampusPort(mCampusName)));
            System.out.println("Server for " + mCampusName + " is running...");
            while (notKilled) {
                try {
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    mSocket.receive(packet);

                    new ProcessThread(packet).start();
                } catch (SocketException e) {
                }
            }
        } finally {
            if (mSocket != null)
                mSocket.close();
            mSocket = null;
        }
    }

    public void stop() {
        notKilled = false;
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    @Override
    public String createRoom(String adminID, int roomNumber, String date, String[] timeSlots) {
        String response;
        try {
            synchronized (mData) {
                HashMap<Integer, List<RoomRecord>> roomData = mData.get(date);
                if (roomData == null) roomData = new HashMap<>();

                List<RoomRecord> records = roomData.get(roomNumber);
                if (records == null) records = new ArrayList<>();

                for (String timeSlot : timeSlots) {
                    if (records.isEmpty() || !RoomRecord.hasTimeSlot(records, timeSlot)) {
                        RoomRecord record = new RoomRecord(timeSlot, null);
                        records.add(record);
                    }
                }

                roomData.put(roomNumber, records);
                this.mData.put(date, roomData);
                response = "Time slots created for provided date and room.";


            }
        } catch (Exception e) {
            response = e.getMessage();
        }

        String logMessage = "Date: " + new Date().toString();
        logMessage += "\nRequest Type: Create Room";
        logMessage += "\nRequested by: " + adminID;
        logMessage += "\nParameters:";
        logMessage += "\n\tRoom number: " + roomNumber;
        logMessage += "\n\tDate: " + date;
        logMessage += "\n\tTime slots: " + timeSlots;
        logMessage += "\nServerPublisher response: " + response;
        Util.writeLog(mCampusName + "-server.log", logMessage);

        return response;
    }

    @Override
    public String deleteRoom(String adminID, int roomNumber, String date, String[] timeSlots) {
        String response;
        try {
            synchronized (mData) {
                HashMap<Integer, List<RoomRecord>> roomData = mData.get(date);
                if (roomData == null) {
                    response = "No rooms available for given date.";
                } else {
                    List<RoomRecord> records = roomData.get(roomNumber);
                    if (records == null) {
                        response = "No room records available for given room number";
                    } else {
                        for (String timeSlot : timeSlots) {
                            for (Iterator<RoomRecord> iterator = records.iterator(); iterator.hasNext(); ) {
                                RoomRecord record = iterator.next();
                                if (record.mTimeSlot.equals(timeSlot)) {
                                    if (record.mBookedBy != null) {
                                        int oldCount = mStudentData.get(record.mBookedBy);
                                        mStudentData.put(record.mBookedBy, oldCount - 1);
                                    }
                                    iterator.remove();
                                }
                            }
                        }

                        roomData.put(roomNumber, records);
                        this.mData.put(date, roomData);
                        response = "Time slots deleted for selected date and room";
                    }
                }
            }
        } catch (Exception e) {
            response = e.getMessage();
        }

        String logMessage = "Date: " + new Date().toString();
        logMessage += "\nRequest Type: Delete Room";
        logMessage += "\nRequested by: " + adminID;
        logMessage += "\nParameters:";
        logMessage += "\n\tRoom number: " + roomNumber;
        logMessage += "\n\tDate: " + date;
        logMessage += "\n\tTime slots: " + timeSlots;
        logMessage += "\nServerPublisher response: " + response;
        Util.writeLog(mCampusName + "-server.log", logMessage);

        return response;
    }

    @Override
    public String bookRoom(String studentID, String campusName, int roomNumber, String date, String timeSlot) {
        String bookingID, response = "error";
        if (!campusName.equals(mCampusName)) {
            String logMessage = "Date: " + new Date().toString();
            logMessage += "\nRequest Type: Book Room";
            logMessage += "\nServerPublisher response: redirected request to " + campusName + "server";
            Util.writeLog(mCampusName + "-server.log", logMessage);
            return redirectBookRequest(studentID, campusName, roomNumber, date, timeSlot);
        } else {
            int count = (mStudentData.get(studentID) == null) ? 0 : mStudentData.get(studentID);
            if (count < 3) {
                synchronized (mData) {
                    HashMap<Integer, List<RoomRecord>> roomData = mData.get(date);
                    if (roomData != null) {
                        List<RoomRecord> records = roomData.get(roomNumber);
                        if (records != null && !records.isEmpty()) {
                            for (RoomRecord record : records) {
                                if (record.mTimeSlot.equals(timeSlot) && record.mBookedBy == null) {
                                    record.setBookedBy(studentID);
                                    bookingID = campusName + "B" + String.format("%04d", ++mBookingId);
                                    record.setBookingID(bookingID);

                                    mStudentData.put(studentID, count + 1);
                                    response = "Room booked with bookingID of " + bookingID;
                                }
                            }
                        }
                    }
                }
            }
        }

        String logMessage = "Date: " + new Date().toString();
        logMessage += "\nRequest Type: Book Room";
        logMessage += "\nRequested by: " + studentID;
        logMessage += "\nParameters:";
        logMessage += "\n\tStudentID: " + studentID;
        logMessage += "\n\tcampuses name: " + campusName;
        logMessage += "\n\tRoom number: " + roomNumber;
        logMessage += "\n\tDater: " + date;
        logMessage += "\n\tTime slot: " + timeSlot;
        logMessage += "\nServerPublisher response: " + ((response.equals("error")) ? "Could not perform your request." : response);
        Util.writeLog(mCampusName + "-server.log", logMessage);

        return response;
    }

    @Override
    public String getAvailableTimeSlots(String studentID, String date) {
        return getAvailableTimeSlots(studentID, mCampusName, date);
    }

    private String getAvailableTimeSlots(String studentID, String campus, String date) {
        if (hasError) return "KKL0 DVL2 WST4";

        String returnMessage;
        int count = 0;
        synchronized (mData) {
            if (mData.get(date) != null) {
                for (Integer room : mData.get(date).keySet()) {
                    if (mData.get(date).get(room) != null) {
                        for (RoomRecord record : mData.get(date).get(room)) {
                            if (record.mBookingID == null) count++;
                        }
                    }
                }
            }
        }
        returnMessage = (mCampusName + count);
        if (mCampusName.equals(campus)) {
            returnMessage += " " + redirectLookUpRequest(studentID, campus, date);
        }

        String logMessage = "Date: " + new Date().toString();
        logMessage += "\nRequest Type: Get available time slots";
        logMessage += "\nRequested by: " + studentID;
        logMessage += "\nParameters:";
        logMessage += "\n\tDate: " + date;
        logMessage += "\nServerPublisher response: " + returnMessage;
        Util.writeLog(mCampusName + "-server.log", logMessage);

        return returnMessage;
    }

    @Override
    public String cancelBooking(String studentID, String bookingID) {
        String campusName = bookingID.substring(0, 3);
        String response = "error";
        if (!campusName.equals(mCampusName)) {
            String logMessage = "Date: " + new Date().toString();
            logMessage += "\nRequest Type: Cancel booking";
            logMessage += "\nServerPublisher response: redirected request to " + campusName + "server";
            Util.writeLog(mCampusName + "-server.log", logMessage);
            return redirectBookCancelRequest(studentID, bookingID);
        } else {
            synchronized (mData) {
                for (String dateKey : mData.keySet()) {
                    HashMap<Integer, List<RoomRecord>> roomData = mData.get(dateKey);
                    for (Integer roomKey : roomData.keySet()) {
                        for (RoomRecord record : roomData.get(roomKey)) {
                            if (studentID.equals(record.mBookedBy) && bookingID.equals(record.mBookingID)) {
                                record.setBookedBy(null);
                                record.setBookingID(null);
                                if (mStudentData.get(studentID) != null) {
                                    int count = mStudentData.get(studentID);
                                    mStudentData.put(studentID, count - 1);
                                }
                                response = "Your booking with bookingID " + bookingID + " has been canceled.";
                            }
                        }
                    }
                }
            }
        }

        String logMessage = "Date: " + new Date().toString();
        logMessage += "\nRequest Type: Cancel Booking";
        logMessage += "\nRequested by: " + studentID;
        logMessage += "\nParameters:";
        logMessage += "\n\tStudentID: " + studentID;
        logMessage += "\n\tBooking ID: " + bookingID;
        logMessage += "\nServerPublisher response: " + ((response.equals("error")) ? "Could not perform your request." : response);
        Util.writeLog(mCampusName + "-server.log", logMessage);

        return response;
    }

    @Override
    public String changeReservation(String studentID, String bookingID, String campusName, int roomNumber, String date, String timesSlot) {
        String bookResponse = "", cancelResponse = "";
        boolean flag = false;
        boolean available = isRoomAvailable(campusName, roomNumber, date, timesSlot);
        if (available) {
            cancelResponse = cancelBooking(studentID, bookingID);
            if (cancelResponse.equals("error")) {
                flag = false;
            } else {
                bookResponse = bookRoom(studentID, campusName, roomNumber, date, timesSlot);
                flag = true;
            }
        }

        return flag ? (cancelResponse + "\nAND\n" + bookResponse) : "Could not perform your request";
    }

    private boolean isRoomAvailable(String campusName, int roomNumber, String date, String timeSlot) {
        if (!campusName.equals(mCampusName)) {
            return redirectIsRoomAvailableRequest(campusName, roomNumber, date, timeSlot);
        } else {
            synchronized (mData) {
                HashMap<Integer, List<RoomRecord>> roomData = mData.get(date);
                if (roomData != null) {
                    List<RoomRecord> records = roomData.get(roomNumber);
                    if (records != null && !records.isEmpty()) {
                        for (RoomRecord record : records) {
                            if (record.mTimeSlot.equals(timeSlot) && record.mBookingID == null) return true;
                            else continue;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String redirectBookRequest(String studentId, String campusName, int roomNumber, String date, String timeSlot) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            String message = "book-=" + studentId + "-=" + campusName + "-=" + roomNumber + "-=" + date + "-=" + timeSlot;

            byte[] data = message.getBytes();
            InetAddress host = InetAddress.getByName("localhost");
            int serverPort = Util.getCampusPort(campusName);
            DatagramPacket request = new DatagramPacket(data, data.length, host, serverPort);
            socket.send(request);

            byte[] buffer = new byte[2048];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            return new String(reply.getData()).replace("\0", "");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (socket != null) socket.close();
        }

        return "error";
    }

    private String redirectBookCancelRequest(String studentID, String bookingID) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            String message = "cancel-=" + studentID + "-=" + bookingID;

            byte[] data = message.getBytes();
            InetAddress host = InetAddress.getByName("localhost");
            int serverPort = Util.getCampusPort(bookingID.substring(0, 3));
            DatagramPacket request = new DatagramPacket(data, data.length, host, serverPort);
            socket.send(request);

            byte[] buffer = new byte[2048];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            return new String(reply.getData()).replace("\0", "");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (socket != null) socket.close();
        }

        return "error";
    }

    private String redirectLookUpRequest(String studentID, String campus, String date) {
        String response = "";
        for (String key : new String[]{"KKL", "DVL", "WST"}) {
            if (!key.equals(campus)) {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket();
                    String message = "lookup-=" + studentID + "-=" + campus + "-=" + date;

                    byte[] data = message.getBytes();
                    InetAddress host = InetAddress.getByName("localhost");
                    int serverPort = Util.getCampusPort(key);
                    DatagramPacket request = new DatagramPacket(data, data.length, host, serverPort);
                    socket.send(request);

                    byte[] buffer = new byte[2048];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    socket.receive(reply);

                    response += " " + new String(reply.getData()).replace("\0", "");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                } finally {
                    if (socket != null) socket.close();
                }
            }
        }

        return response;
    }

    private boolean redirectIsRoomAvailableRequest(String campusName, int roomNumber, String date, String timeSlot) {
        DatagramSocket socket = null;
        boolean flag = false;
        try {
            socket = new DatagramSocket();
            String message = "check-=" + campusName + "-=" + roomNumber + "-=" + date + "-=" + timeSlot;

            byte[] data = message.getBytes();
            InetAddress host = InetAddress.getByName("localhost");
            int serverPort = Util.getCampusPort(campusName);
            DatagramPacket request = new DatagramPacket(data, data.length, host, serverPort);
            socket.send(request);

            byte[] buffer = new byte[2048];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            flag = new String(reply.getData()).replace("\0", "").equalsIgnoreCase("true");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (socket != null) socket.close();
        }

        return flag;
    }

    private class ProcessThread extends Thread {

        DatagramPacket packet;

        public ProcessThread(DatagramPacket packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                byte[] request = packet.getData();
                String[] data = new String(request).replace("\0", "").split("-=");
                if (data[0].equals("ping")) {
                    if (!mCampusName.equals("KKL")) { //todo
                        DatagramPacket reply = new DatagramPacket("".getBytes(), 0, packet.getAddress(), packet.getPort());
                        mSocket.send(reply);
                    }
                } else if (data[0].equals("book")) {
                    String response = bookRoom(data[1], data[2], Integer.valueOf(data[3]), data[4], data[5]);
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), InetAddress.getByName("localhost"), 4567);
                    mSocket.send(reply);
                } else if (data[0].equals("cancel")) {
                    String response = cancelBooking(data[1], data[2]);
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), packet.getAddress(), packet.getPort());
                    mSocket.send(reply);
                } else if (data[0].equals("lookup")) {
                    String response = getAvailableTimeSlots(data[1], data[2], data[3]);
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), packet.getAddress(), packet.getPort());
                    mSocket.send(reply);
                } else if (data[0].equals("check")) {
                    String response = String.valueOf(isRoomAvailable(data[1], Integer.valueOf(data[2]), data[3], data[4]));
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), packet.getAddress(), packet.getPort());
                    mSocket.send(reply);
                } else {
                    String response = "Sorry, unknown command.";
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), packet.getAddress(), packet.getPort());
                    mSocket.send(reply);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
