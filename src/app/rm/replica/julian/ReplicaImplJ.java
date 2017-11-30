package app.rm.replica.julian;

import app.Util;
import app.rm.replica.Replica;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplicaImplJ extends Replica<Server> {

    public static HashMap<String, HashMap<String, HashMap<Integer, List<RoomRecord>>>> mData;
    public static HashMap<String, Integer> mStudentData = new HashMap<>();
    public static HashMap<Integer, String[]> processQueue = new HashMap<>();
    private AtomicInteger expectedSequenceNumber = new AtomicInteger(1);

    private static Timer timer = new Timer(true);
    private static TimerTask calendarTask;

    public ReplicaImplJ(int replicaIndex, boolean hasError) {
        super(replicaIndex, hasError);
    }

    @Override
    protected void start(boolean requestData) {
        if (mData == null) mData = new HashMap<>();
        if (requestData) requestData();

        try {
            for (String campus : campuses) {
                if (!requestData) mData.put(campus, new HashMap<>());

                preFillData(campus);
                startServer(campus);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        startWeekCounter();
    }

    @Override
    public void stop() {
        try {
            if (calendarTask != null) calendarTask.cancel();
            for (String campus : serverMap.keySet()) {
                serverMap.get(campus).stop();
            }
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    @Override
    public void restart() {
        hasError = false;
        dataCopied = false;
        stop();
        start(true);
    }

    @Override
    protected void mapJsonToData(String json) {
        try {
            if (mData.isEmpty()) {
                JSONObject jsonData = new JSONObject(json);
                JSONArray database = jsonData.getJSONArray("room_records");
                JSONArray studentData = jsonData.getJSONArray("student_booking");

                expectedSequenceNumber = new AtomicInteger(jsonData.getInt("expected_sequence_number"));

                mStudentData = new HashMap<>();
                for (int i = 0; i < studentData.length(); i++) {
                    JSONObject obj = studentData.getJSONObject(i);
                    mStudentData.put(obj.getString("student_id"), obj.getInt("booking_count"));
                }

                for (int i = 0; i < database.length(); i++) {
                    JSONObject obj = database.getJSONObject(i);
                    JSONArray campusData = obj.getJSONArray("data");
                    HashMap<String, HashMap<Integer, List<RoomRecord>>> data = new HashMap<>();

                    for (int j = 0; j < campusData.length(); j++) {
                        JSONObject campusObj = campusData.getJSONObject(j);
                        HashMap<Integer, List<RoomRecord>> roomData = new HashMap<>();
                        JSONArray rooms = campusObj.getJSONArray("rooms");

                        for (int k = 0; k < rooms.length(); k++) {
                            JSONObject roomObj = rooms.getJSONObject(k);
                            JSONArray recordData = roomObj.getJSONArray("records");
                            List<RoomRecord> records = new ArrayList<>();

                            for (int l = 0; l < recordData.length(); l++) {
                                JSONObject recordObj = recordData.getJSONObject(i);
                                RoomRecord record = new RoomRecord();
                                record.setTimeSlot(recordObj.getString("time_slot"));
                                if (recordObj.has("booked_by")) {
                                    record.setBookedBy(recordObj.getString("booked_by"));
                                    record.setBookingID(recordObj.getString("booking_id"));
                                }
                                records.add(record);
                            }
                            roomData.put(roomObj.getInt("room"), records);
                        }
                        data.put(campusObj.getString("date"), roomData);
                    }
                    mData.put(obj.getString("campus"), data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String mapDataToJson() {
        JSONObject myData = new JSONObject();

        JSONArray database = new JSONArray();
        for (String campus : mData.keySet()) {
            JSONObject campusData = new JSONObject();
            campusData.put("campus", campus);

            JSONArray campusDatabase = new JSONArray();
            for (String date : mData.get(campus).keySet()) {
                JSONObject dateData = new JSONObject();
                dateData.put("date", date);

                JSONArray dateDatabase = new JSONArray();
                for (Integer room : mData.get(campus).get(date).keySet()) {
                    JSONObject roomData = new JSONObject();
                    roomData.put("room", room);

                    JSONArray roomDatabase = new JSONArray();
                    for (RoomRecord record : mData.get(campus).get(date).get(room)) {
                        JSONObject recordData = new JSONObject();
                        recordData.put("time_slot", record.mTimeSlot);
                        recordData.put("booked_by", record.mBookedBy);
                        recordData.put("booking_id", record.mBookingID);
                        roomDatabase.put(recordData);
                    }

                    roomData.put("records", roomDatabase);
                    dateDatabase.put(roomData);
                }

                dateData.put("rooms", dateDatabase);
                campusDatabase.put(dateData);
            }

            campusData.put("data", campusDatabase);
            database.put(campusData);
        }
        myData.put("room_records", database);

        JSONArray bookingCountData = new JSONArray();
        for (String student : mStudentData.keySet()) {
            JSONObject obj = new JSONObject();
            obj.put("student_id", student);
            obj.put("booking_count", mStudentData.get(student));
            bookingCountData.put(obj);
        }
        myData.put("student_booking", bookingCountData);
        myData.put("expected_sequence_number", expectedSequenceNumber.get());

        return myData.toString(4);
    }

    @Override
    public boolean ping(String campus) {
        System.out.println("ping " + campus + " server...");
        try {
            DatagramSocket socket = new DatagramSocket();
            final InetAddress host = InetAddress.getByName("localhost");
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

    private void preFillData(String campus) {
        mData.get(campus);
        HashMap<Integer, List<RoomRecord>> rooms = new HashMap<>();
        List<RoomRecord> records = new ArrayList<>();
        records.add(new RoomRecord("7-9", null));
        records.add(new RoomRecord("9-11", null));
        records.add(new RoomRecord("11-13", null));
        records.add(new RoomRecord("13-15", null));
        rooms.put(200, records);
        mData.get(campus).put("20-10-2017", rooms);
    }

    private void startServer(String campus) {
        Thread thread = new Thread(() -> {
            try {
                Server server = new Server(campus, mData.get(campus), mStudentData, processQueue, expectedSequenceNumber, replicaIndex);
                server.setHasError(hasError);
                serverMap.put(campus, server);
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setName(campus);
        thread.start();
    }

    private void startWeekCounter() {
        calendarTask = new TimerTask() {
            @Override
            public void run() {
                mStudentData.clear();
            }
        };
        timer.scheduleAtFixedRate(calendarTask, 0, 15 * 60 * 1000);
    }
}
