package app.rm.replica.mudra;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class ServerImpl implements ServerInterfaceOperations {

    private static final String id = null;
    private int expectedSequenceNumber;

    // server details
    public enum ServerDetails {
        KKL("KRIKLAND-SERVER", "KKL", "127.0.0.1", 1092, 0001), DVL("DORVAL-SERVER", "DVL", "127.0.0.1", 1091, 0001), WST("WESTMOUNT-SERVER", "WST", "127.0.0.1", 1093, 0001);

        public String host, serverName, tag;
        public int port;
        private int expectedSequenceNumber;

        private ServerDetails(String serverName, String tag, String host, int port, int expectedSequenceNumber) {
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
    private int idNo = 0001;

    // HashMap database
    private final HashMap<String, HashMap<String, HashMap<String, Record>>> roomRecords = new HashMap<String, HashMap<String, HashMap<String, Record>>>();
    private HashMap<Integer, String[]> processQueue;

    // current server configuration
    public ServerDetails currentServer;

    // Lock Object to perform synchronization
    private final Object lock = new Object();

    // List to hold details of all the server
    public ArrayList<ServerDetails> concurrentServerList = new ArrayList<>();   // list of different servers

    /**
     * Parameterized constructor
     *
     * @param currentServer to configure our server
     */
    public ServerImpl(ServerDetails currentServer) {
        super();
        this.currentServer = currentServer;
        init();
    }

    public void start(){

    }

    /**
     * initializes server object with default values also creates log files for
     * the server initializes File Reader and Writer
     */
    private void init() {
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
     * Creates a new Connection Listener Object using multi-threading This
     * listener opens UDP socket to get requests from other server
     */
    private void activateListener() {
        Thread t = new Thread(new ConnectionListener(this));
        t.start();
    }

    public String createRoom(String roomNo, String date, String timeSlot) {
        int flag = 1;
        synchronized (lock) {
            if (!(roomRecords.containsKey(date))) {
                roomRecords.put(date, new HashMap<String, HashMap<String, Record>>());
                roomRecords.get(date).put(roomNo, new HashMap<String, Record>());
                roomRecords.get(date).get(roomNo).put(timeSlot, null);
            } else if (!(roomRecords.get(date).containsKey(roomNo))) {
                roomRecords.get(date).put(roomNo, new HashMap<String, Record>());
                roomRecords.get(date).get(roomNo).put(timeSlot, null);
            } else if (!(roomRecords.get(date).get(roomNo).containsKey(timeSlot))) {
                roomRecords.get(date).get(roomNo).put(timeSlot, null);
                writeToLogFile("success: Time slots created for provided date and room." + id);
            } else {
                System.out.println("failed: Room already exists");
                flag = 0;
            }
            if (flag == 1)
                System.out.println("Room created");
            return id;
        }
    }

    public String deleteRoom(String roomNo, String date, String timeSlot) {
        synchronized (lock) {
            if (roomRecords.containsKey(date)) {
                if (roomRecords.get(date).containsKey(roomNo)) {
                    if (roomRecords.get(date).get(roomNo).containsValue(timeSlot)) {
                        roomRecords.get(date).get(roomNo).remove(timeSlot, null);
                        System.out.println("Room deleted");
                        writeToLogFile("Room deleted by Admin :" + id);
                    }
                }
            } else {
                System.out.println("Room does not exist");
            }
            return id;
        }
    }

    public String bookRoom(String campusName, String roomNo, String date, String timeSlot, String studentID, String bookingID) {
        String id = currentServer.tag.charAt(0) + "B" + idNo;
        idNo++;
        synchronized (lock) {
            if (roomRecords.get(date).get(roomNo).containsKey(timeSlot == null)) {
                roomRecords.get(date).get(roomNo).put(timeSlot, new Record(id, studentID));
                System.out.println("Room Booked");
                writeToLogFile("success: Room booked with bookingID of " + id);
            } else if (roomRecords.get(date).get(roomNo).containsKey(timeSlot != null)) {
                System.out.println("Booking Already exists");
            }
            return id;
        }
    }

    public String getTimeSlots(String date) {
        Stream<Object> slotList;
        slotList = roomRecords.entrySet().stream().map((e) -> e.getValue());
        String list = currentServer.tag + slotList;
        synchronized (lock) {
            try {
                for (ServerDetails s : concurrentServerList) {
                    DatagramSocket aSocket = new DatagramSocket();
                    InetAddress aHost = InetAddress.getByName(s.host);

                    String req = currentServer.serverName + ":" + "get Room Count";
                    DatagramPacket request = new DatagramPacket(req.getBytes(), req.length(), aHost, s.port);
                    aSocket.send(request);

                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(reply);
                    list += "," + (new String(reply.getData()).trim());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Avialable time Slots" + list);
            writeToLogFile("Student(" + id + ") - requested time slots : " + list);
            return list;
        }
    }

    public boolean cancelRoom(String bookingID, String studentID) {
        synchronized (lock) {
            String timeSlot = null;
            String roomNo = null;
            String date = null;
            if (roomRecords.get(date).get(roomNo).containsKey(timeSlot)) {
                System.out.println("Booking doesnot exist");
                writeToLogFile("failed : booking from your id doesnot exist " + id);
                return true;
            } else if (roomRecords.get(date).get(roomNo).containsKey(timeSlot)) {
                roomRecords.get(date).get(roomNo).remove(timeSlot, null);
                System.out.println("Room Booking deleted");
                writeToLogFile("success: Booking successfully cancelled for Student " + id);
            }
        }
        return false;
    }

    public String changeBooking(String bookingID, String newCampusName, String newroomNo, String newtimeSlot) {
        Object recordId = null;
        String managerId = null;
        synchronized (lock) {
            Record r = null;
            for (Entry<String, HashMap<String, HashMap<String, Record>>> e : roomRecords.entrySet())
                if (r.id.equals(recordId)) {
                    String response;
                    Object toServer = null;
                    for (ServerDetails s : concurrentServerList)
                        if (s.serverName.equals(toServer) || s.tag.equals(toServer))
                            try {
                                DatagramSocket aSocket = new DatagramSocket();
                                InetAddress aHost = InetAddress.getByName(s.host);

                                String req = currentServer.serverName + ":" + "transfer" + ":" + e.getValue().toString();
                                DatagramPacket request = new DatagramPacket(req.getBytes(), req.length(), aHost, s.port);
                                aSocket.send(request);

                                byte[] buffer = new byte[1000];
                                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                                aSocket.receive(reply);

                                response = (new String(reply.getData()).trim());
                                if (response.equals("success")) {
                                    roomRecords.remove(e.getKey(), e.getValue());
                                    response = "Successfully transferred Record(" + recordId + ") from " + currentServer.serverName + " to " + toServer;
                                    writeToLogFile("from server(" + currentServer.serverName + ") " + response);
                                } else {
                                    response = "Failed to transfer record(" + recordId + ") from " + currentServer.serverName + " to " + toServer;
                                    writeToLogFile("from server(" + currentServer.serverName + ") " + response);
                                }
                                return response;
                            } catch (Exception ex) {
                                response = currentServer.serverName + " faced an unexpected issue - couldn't connect to transfer record(" + recordId + ") to" + toServer;
                                writeToLogFile("from server(" + currentServer.serverName + ") - " + response);
                                return response;
                            }
                }
        }
        String response = "Record ID(" + recordId + ") does not exist";
        writeToLogFile("from server(" + currentServer.serverName + ") - " + response);
        return response;
    }

    /**
     * Returns the size of HashMap size : count of all records in the HashMap
     * this method is called by the ConnectionListener when it receives the
     * 'getRecordCount' request from another server via UDP
     *
     * @param ip specifies the IP address of the server making request to get
     *           count
     * @return server name and HashMap record count merged in single string
     */
    public String getSize(String ip) {
        // return the count
        writeToLogFile(ip + " - requested records count : " + roomRecords.size());
        int size = 0;
        synchronized (lock) {
            size = roomRecords.entrySet().stream().map((e) -> e.getValue().size()).reduce(size, Integer::sum);
        }
        return currentServer.tag + size;
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

    private void handleRequest(String[] data, InetAddress host, int port) throws IOException {
        int seq = Integer.valueOf(data[0]);
        processQueue.put(seq, data);
        if (expectedSequenceNumber == seq) {
            processRequest(processQueue.get(expectedSequenceNumber), host, port, true);
        }
    }

    private void processRequest(String[] data, InetAddress host, int port, boolean fromQueue) throws IOException {
        DatagramSocket aSocket = null;
        if (data[0].equals("ping")) {
            if (currentServer.tag.equals("KKL") || currentServer.tag.equals("DVL") || currentServer.tag.equals("WST")) {
                if (data[1].equals("create")) {
                    String response = createRoom(data[2], Integer.valueOf(data[3]), data[4], data[5].split(","));
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
                    aSocket.send(reply);
                } else if (data[1].equals("delete")) {
                    String response = deleteRoom(data[2], Integer.valueOf(data[3]), data[4], data[5].split(","));
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
                    aSocket.send(reply);
                } else if (data[1].equals("cancel")) {
                    String response = cancelBooking(data[2], data[3]);
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
                    aSocket.send(reply);
                } else if (data[1].equals("availability")) {
                    String response = getTimeSlots(data[2], data[3], data[4]);
                    DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), host, port);
                    aSocket.send(reply);
                } else if (data[1].equals("book")) {
                } else if (data[1].equals("change booking")) {
                }

                if (fromQueue) {
                    expectedSequenceNumber++;
                    if (processQueue.keySet().contains(expectedSequenceNumber))
                        processRequest(processQueue.get(expectedSequenceNumber), host, port, true);
                }
            }
        }
    }

    private String getTimeSlots(String string, String string2, String string3) {
        // TODO Auto-generated method stub
        return null;
    }

    private String cancelBooking(String string, String string2) {
        // TODO Auto-generated method stub
        return null;
    }

    private String deleteRoom(String adminID, Integer valueOf, String date, String[] split) {
        // TODO Auto-generated method stub
        return null;
    }

    private String createRoom(String adminID, Integer valueOf, String date, String[] split) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean createRoom(String adminID, int roomNo, String date, String timeSlot) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteRoom(String adminID, int roomNo, String date, String timeSlot) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean bookRoom(String studentID, String campusName, int roomNo, String date, String timeSlot) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getTimeSlots(String studentID, String date) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean changeBooking(String studentID, String bookingID, String campusName, int roomNo, String date,
                                 String timeSlot) {
        // TODO Auto-generated method stub
        return false;
    }
}
