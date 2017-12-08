package app.rm.replica.mudra;

import app.Util;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerImpl {

    private static final String id = null;
    private static String NULL = null;
    private AtomicInteger expectedSequenceNumber;
    ConnectionListener cl;
    public String bookingReply = "", cancelReply = "";
    private HashMap<Integer, String[]> processQueue = new HashMap<>();
    String result = "";
    boolean hasError = false;

    // server details
    public enum ServerDetails {
        KKL("KRIKLAND-SERVER", "KKL", Util.REPLICA_HOSTS[0], Util.KKL_PORT[0], new AtomicInteger(1)), DVL(
                "DORVAL-SERVER", "DVL", Util.REPLICA_HOSTS[0], Util.DVL_PORT[0], new AtomicInteger(1)), WST(
                "WESTMOUNT-SERVER", "WST", Util.REPLICA_HOSTS[0], Util.WST_PORT[0], new AtomicInteger(1));

        public String host, serverName, tag;
        public int port;
        private AtomicInteger expectedSequenceNumber;

        private ServerDetails(String serverName, String tag, String host, int port,
                              AtomicInteger expectedSequenceNumber) {
            this.serverName = serverName;
            this.tag = tag;
            this.host = host;
            this.port = port;
            this.expectedSequenceNumber = expectedSequenceNumber;
        }
    }

    ;

    // Reference to log file
    private File logFile;

    // File Writer
    private PrintWriter pw;

    // static count variable to insert record with unique names
    private int idNo = 0;

    // HashMap database
    private HashMap<String, HashMap<String, HashMap<String, Record>>> roomRecords;

    // current server configuration
    public ServerDetails currentServer;

    // Lock Object to perform synchronization
    private final Object lock = new Object();

    // List to hold details of all the server
    public ArrayList<ServerDetails> concurrentServerList = new ArrayList<>(); // list of different servers

    /**
     * Parameterized constructor
     *
     * @param currentServer to configure our server
     * @param hasError
     */
    public ServerImpl(ServerDetails currentServer, HashMap<String, HashMap<String, HashMap<String, Record>>> roomRecords, HashMap<Integer, String[]> processQueue,
                      AtomicInteger expectedSequenceNumber, boolean hasError) {
        super();
        this.currentServer = currentServer;
        this.processQueue = processQueue;
        this.expectedSequenceNumber = expectedSequenceNumber;
        this.roomRecords = roomRecords;
        this.hasError = hasError;
        // init();
    }

    /**
     * initializes server object with default values also creates log files for the
     * server initializes File Reader and Writer
     */
    public void init() {
        try {
            // create log file
            logFile = new File("log_" + currentServer.tag + ".log");
            if (!logFile.exists())
                logFile.createNewFile();
            pw = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
        } catch (IOException exc) {
            System.out.println("Error in creating log File: " + exc.toString());
        }

        getServerList();
        activateListener();

        System.out.println(currentServer.serverName + " Started...");
    }

    /**
     * This method gives list of all the server used by us
     */
    private void getServerList() {
        concurrentServerList.clear();
        for (ServerDetails value : ServerDetails.values())
            if (value != currentServer)
                concurrentServerList.add(value);
    }

    /**
     * Creates a new Connection Listener Object using multi-threading This listener
     * opens UDP socket to get requests from other server
     */
    public void activateListener() {
        Thread t = new Thread(new ConnectionListener(this));
        t.start();
    }

    public void stop() {
        cl = new ConnectionListener(this);
        cl.stop();
    }

    public void restart() {
        stop();
        activateListener();
    }

    public ServerDetails[] getOtherServer() {
        ServerDetails[] serverDatas = ServerDetails.values();
        for (int i = 0; i < serverDatas.length; i++)
            if (serverDatas[i].equals(currentServer))
                serverDatas[i] = null;
        return serverDatas;
    }

    private DatagramPacket contactOtherServers(ServerDetails s, String query) throws IOException {
        DatagramSocket aSocket = new DatagramSocket();
        InetAddress aHost = InetAddress.getByName(s.host);

        DatagramPacket request = new DatagramPacket(query.getBytes(), query.length(), aHost, s.port);
        aSocket.send(request);

        byte[] buffer = new byte[1000];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        aSocket.receive(reply);

        return reply;
    }

    public String createRoom(String adminID, String roomNo, String date, String timeSlot) {
        int flag = 1;
        synchronized (lock) {
            String[] slots = timeSlot.split(",");
            for (int i = 0; i < slots.length; i++) {
                if (!(roomRecords.containsKey(date))) {
                    roomRecords.put(date, new HashMap<String, HashMap<String, Record>>());
                    roomRecords.get(date).put(roomNo, new HashMap<String, Record>());
                    roomRecords.get(date).get(roomNo).put(slots[i], new Record());
                    result = "success";
                } else if (!(roomRecords.get(date).containsKey(roomNo))) {
                    roomRecords.get(date).put(roomNo, new HashMap<String, Record>());
                    roomRecords.get(date).get(roomNo).put(slots[i], new Record());
                    result = "success";
                } else if (!(roomRecords.get(date).get(roomNo).containsKey(slots[i]))) {
                    roomRecords.get(date).get(roomNo).put(slots[i], new Record());
                    result = "success";
                    writeToLogFile("success: Time slots created for provided date and room." + id);
                } else {
                    result = "failed";
                    System.out.println("failed: Room already exists");
                    flag = 0;
                }
                if (flag == 1)
                    System.out.println("Room created");
            }
            return result;
        }
    }

    public String deleteRoom(String adminID, String roomNo, String date, String timeSlot) {
        synchronized (lock) {
            if (roomRecords.containsKey(date) && roomRecords.get(date).containsKey(roomNo)) {
                if (roomRecords.get(date).get(roomNo).containsKey(timeSlot)) {
                    Record record = roomRecords.get(date).get(roomNo).get(timeSlot);
                    if (record != null && record.bookedBy != null) {
                        int count = Record.studentBookingCounter.get(record.bookedBy);
                        Record.studentBookingCounter.put(record.bookedBy, count - 1);
                    }
                    roomRecords.get(date).get(roomNo).remove(timeSlot);
                    result = "success";
                    System.out.println("Room deleted");
                    writeToLogFile("Room deleted by Admin :" + id);
                } else {
                    result = "fail";
                    System.out.println("Time Slot does not exist");
                }
            } else {
                result = "fail";
                System.out.println("Room does not exist");
            }
            return result;
        }
    }

    public String bookRoom(String studentID, String campusName, String roomNo, String date, String timeSlot) {
        int limit = Record.checkLimit(studentID);
        String result = "";
        synchronized (lock) {
            if (limit >= 3) {
                return "failed";
            } else if (!currentServer.tag.equals(campusName)) {
                for (ServerDetails s : getOtherServer()) {
                    if (s != null && s.tag.equals(campusName)) {
                        String message = "book" + "-=" + studentID + "-=" + s.tag + "-=" + roomNo + "-=" + date + "-=" + timeSlot + "-=";
                        DatagramPacket reply = null;
                        try {
                            reply = contactOtherServers(s, message);
                            result = new String(reply.getData()).substring(3, 21);
                        } catch (IOException e) {
                            e.printStackTrace();
                            result = "failed";
                        }
                        break;
                    }
                }
            } else if (roomRecords.get(date).get(roomNo).get(timeSlot).bookedBy == null) {
                String id = campusName + "B" + String.format("%04d", ++idNo);
                roomRecords.get(date).get(roomNo).put(timeSlot, new Record(id, studentID));
                result = "success:" + id;
                System.out.println("Room Booked");
                writeToLogFile("success: Room booked with bookingID of " + id);
            } else if (roomRecords.get(date).get(roomNo).get(timeSlot).bookedBy != null) {
                result = "failed";
                System.out.println("Booking Already exists");
            }
            if (result.contains("success")) {
                Record.studentBookingCounter.put(studentID, limit + 1);
            }
        }
        return result;
    }

    private int getLocalCount(String date) {
        int currentCount = 0;
        if (roomRecords.containsKey(date)) {
            for (Entry<String, HashMap<String, Record>> entry2 : roomRecords.get(date).entrySet()) {
                String roomNo = entry2.getKey();
                for (Entry<String, Record> entry3 : entry2.getValue().entrySet()) {
                    String timeSlot = entry3.getKey();
                    if (entry3.getValue() != null && entry3.getValue().bookedBy == null) {
                        currentCount++;
                    }
                }
            }
        }
        return currentCount;
    }

    public String getTimeSlots(String date) {
        if (hasError) {
            return "failed";
        }
        String list = currentServer.tag + getLocalCount(date);
        synchronized (lock) {
            try {
                for (ServerDetails s : concurrentServerList) {
                    if (s != null) {
                        String req = "availability" + "-=" + date;
                        DatagramPacket reply = contactOtherServers(s, req);
                        String s1 = new String(reply.getData()).substring(3, 6) + new String(reply.getData()).substring(7, 8);
                        list += " " + s1;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Avialable time Slots:" + list);
            writeToLogFile("Student(" + id + ") - requested time slots : " + list);
            return "success: " + list;
        }
    }

    public String cancelRoom(String studentID, String bookingID) {
        Integer tmp = Record.studentBookingCounter.get(studentID);
        String campusName = bookingID.substring(0, 3);
        String issuccess = null;
        synchronized (lock) {
            for (Entry<String, HashMap<String, HashMap<String, Record>>> entry1 : roomRecords.entrySet()) {
                String date = entry1.getKey();
                for (Entry<String, HashMap<String, Record>> entry2 : entry1.getValue().entrySet()) {
                    String roomNo = entry2.getKey();
                    for (Entry<String, Record> entry3 : entry2.getValue().entrySet()) {
                        String timeSlot = entry3.getKey();if (entry3.getValue().id.matches(bookingID) && entry3.getValue().bookedBy != null && entry3.getValue().id.matches(bookingID) && entry3.getValue().bookedBy.matches(studentID)) {
                            roomRecords.get(date).get(roomNo).put(timeSlot, null);
                            System.out.println("Room cancelled.");
                            result = "success";
                        } else {
                            System.out.println("No booking exists. Cancel failed");
                            result = "failed";
                        }
                    }
                }
            }
            return result;
        }
    }

    public String changeBooking(String studentID, String bookingID, String newCampusName, String newroomNo, String date,
                                String newtimeSlot) {
        for (Entry<String, HashMap<String, HashMap<String, Record>>> entry1 : roomRecords.entrySet()) {
            String date1 = entry1.getKey();
            for (Entry<String, HashMap<String, Record>> entry2 : entry1.getValue().entrySet()) {
                String roomNo = entry2.getKey();
                for (Entry<String, Record> entry3 : entry2.getValue().entrySet()) {
                    String timeSlot = entry3.getKey();
                    try {
                        if (entry3.getValue().id.matches(bookingID) && entry3.getValue().bookedBy.matches(studentID)) {
                            roomRecords.get(date1).get(roomNo).put(timeSlot, null);
                            System.out.println("Room cancelled");
                            result = bookRoom(studentID, newCampusName, newroomNo, date, newtimeSlot);
                            System.out.println("Changed Booking Successful");
                            System.out.println(result);
                            return result;
                        } else {
                            System.out.println("No booking exists. Cancel failed");
                            result = "failed";
                        }
                    }catch (NullPointerException e){

                    }
                }
            }
        }
        System.out.println(result);
        return result;
    }

    /**
     * a common method to record the logs into the log file
     *
     * @param msg specifies the message to be written into the log file
     */
    synchronized public void writeToLogFile(String msg) {
        try {
            if (pw == null)
                return;
            // print the time and the message to log file
            pw.println(Calendar.getInstance().getTime().toGMTString() + " - " + msg);
            pw.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void handleRequest(String[] data, InetAddress host, int port) throws IOException {
        String ack_message = "ack-=0-=" + data[0];
        DatagramPacket ack_packet = new DatagramPacket(ack_message.getBytes(), ack_message.length(), InetAddress.getByName(Util.SEQUENCER_HOST), Util.SEQUENCER_PORT);
        new DatagramSocket().send(ack_packet);

        System.out.println("handleRequest" + Arrays.deepToString(data));
        int seq = Integer.valueOf(data[0]);
        processQueue.put(seq, Arrays.copyOfRange(data, 2, data.length));
        if (expectedSequenceNumber.get() == seq) {
            processRequest(processQueue.get(expectedSequenceNumber.get()), host, port, true);
        }
    }

    public void processRequest(String[] data, InetAddress host, int port, boolean fromQueue) throws IOException {
        DatagramSocket aSocket = new DatagramSocket();
        System.out.println("processRequest" + Arrays.deepToString(data));
        if (data[0].equals("ping")) {
            DatagramPacket reply = new DatagramPacket("".getBytes(), "".length(), host, port);
            aSocket.send(reply);
        } else if (data[0].equals("create")) {
            String response = createRoom(data[1], data[2], data[3], data[4]);
            response = "0-=" + response;
            DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
            aSocket.send(reply);
        } else if (data[0].equals("delete")) {
            String response = deleteRoom(data[1], data[2], data[3], data[4]);
            response = "0-=" + response;
            DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
            aSocket.send(reply);
        } else if (data[0].equals("cancel")) {
            String response = cancelRoom(data[1], data[2]);
            response = "0-=" + response;
            DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
            aSocket.send(reply);
        } else if (data[0].equals("availability")) {
            String response;
            if (fromQueue)
                response = getTimeSlots(data[2]);
            else
                response = currentServer.tag + " " + getLocalCount(data[1]);
            response = "0-=" + response;
            DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
            aSocket.send(reply);
        } else if (data[0].equals("book")) {
            String response = bookRoom(data[1], data[2], data[3], data[4], data[5]);
            response = "0-=" + response;
            DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
            aSocket.send(reply);
        } else if (data[0].equals("change")) {
            String response = changeBooking(data[1], data[2], data[3], data[4], data[5], data[6]);
            response = "0-=" + response;
            DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
            aSocket.send(reply);
        }

        if (fromQueue) {
            expectedSequenceNumber.incrementAndGet();
            if (processQueue.keySet().contains(expectedSequenceNumber.get()))
                processRequest(processQueue.get(expectedSequenceNumber.get()), host, port, true);
        }
    }
}