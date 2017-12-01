package app.rm.replica.rashmi;

import app.rm.replica.Replica;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplicaImplR extends Replica<Server> {
    public static HashMap<String, HashMap<Integer, List<RoomRecordClass>>> KKL_data = new HashMap<>();
    public static HashMap<String, HashMap<Integer, List<RoomRecordClass>>> WST_data = new HashMap<>();
    public static HashMap<String, HashMap<Integer, List<RoomRecordClass>>> DVL_data = new HashMap<>();

    public static HashMap<String, Integer> student_booking = new HashMap<>();
    public static HashMap<Integer, String[]> queue;
    public static AtomicInteger expected = new AtomicInteger(1);
    private static Timer timer = new Timer(true);

    public ReplicaImplR(int replicaIndex, boolean hasError) {
        super(replicaIndex, hasError);
        queue = new HashMap<>();
    }

    @Override
    protected void start(boolean requestData) {
        TimerTask timer_task = new TimerTask() {
            @Override
            public void run() {
                student_booking.clear();
            }
        };
        timer.scheduleAtFixedRate(timer_task, 0, 1000 * 60 * 60);//1hr
        for (String campus : campuses) {
            Server server;
            if (campus.equals("KKL")) {
                server = new Server(KKL_data, student_booking, campus, queue, expected, replicaIndex);
            } else if (campus.equals("DVL")) {
                server = new Server(DVL_data, student_booking, campus, queue, expected, replicaIndex);
            } else {
                server = new Server(WST_data, student_booking, campus, queue, expected, replicaIndex);
            }
            serverMap.put(campus, server);
            server.start();
        }
    }

    @Override
    public void stop() {
        for (String campus : serverMap.keySet()) {
            serverMap.get(campus).stop();
        }

    }

    @Override
    public void restart() {
        dataCopied = false;
        stop();
        start();
    }

    @Override
    protected void mapJsonToData(String json) {
        JSONObject database = new JSONObject(json);

        expected = new AtomicInteger(database.getInt("expected_sequence_number"));

        JSONArray room_records_array = database.getJSONArray("room_records");
        JSONArray student_booking_array = database.getJSONArray("student_booking");

        student_booking = new HashMap<>();

        HashMap<String, HashMap<Integer, List<RoomRecordClass>>> campus_data = new HashMap<>();


        for (int stu_record_num = 0; stu_record_num < student_booking_array.length(); stu_record_num++) {
            JSONObject student = student_booking_array.getJSONObject(stu_record_num);
            student_booking.put(student.getString("student_id"), student.getInt("booking_id"));
        }

        for (int campus_record_num = 0; campus_record_num < room_records_array.length(); campus_record_num++) {
            String campus = room_records_array.getJSONObject(campus_record_num).get("campus").toString();


            JSONObject room_records_object = room_records_array.getJSONObject(campus_record_num);
            JSONArray data = room_records_object.getJSONArray("data");
            for (int campus_data_num = 0; campus_data_num < data.length(); campus_data_num++) {
                JSONObject data_object = data.getJSONObject(campus_data_num);
                String date = data_object.getString("date");
                JSONArray rooms_array = data_object.getJSONArray("rooms");
                HashMap<Integer, List<RoomRecordClass>> room_data = new HashMap<>();
                for (int rooms_num = 0; rooms_num < rooms_array.length(); rooms_num++) {
                    JSONObject rooms_object = rooms_array.getJSONObject(rooms_num);
                    int room_number = (rooms_object.getInt("room"));
                    JSONArray records_array = rooms_object.getJSONArray("records");
                    List<RoomRecordClass> room_record_list = new ArrayList<>();
                    for (int records_num = 0; records_num < records_array.length(); records_num++) {
                        JSONObject records_object = records_array.getJSONObject(records_num);
                        String time_slot = records_object.getString("time_slot");
                        String booked_by = null;
                        String booking_id = null;
                        if (records_object.has("booked_by")) {
                            booked_by = records_object.getString("booked_by");
                            booking_id = records_object.getString("booking_id");
                        }
                        RoomRecordClass obj = new RoomRecordClass(time_slot, booked_by, booking_id);
                        room_record_list.add(obj);
                    }

                    room_data.put(room_number, room_record_list);
                }

                campus_data.put(date, room_data);
            }

            if (campus.equals("KKL")) {
                KKL_data = campus_data;
            } else if (campus.equals("DVL")) {
                DVL_data = campus_data;
            } else {
                WST_data = campus_data;
            }
        }
    }

    @Override
    protected String mapDataToJson() {
        JSONObject database = new JSONObject();
        JSONArray data_array = new JSONArray();
        for (String campus : campuses) {
            JSONObject campusData = new JSONObject();
            campusData.put("campus", campus);
            JSONArray data = new JSONArray();
            HashMap<String, HashMap<Integer, List<RoomRecordClass>>> campus_data;
            if (campus.equals("KKL")) {
                campus_data = KKL_data;
            } else if (campus.equals("DVL")) {
                campus_data = DVL_data;
            } else {
                campus_data = WST_data;
            }
            for (String date : campus_data.keySet()) { //hashmap ko date ko bhitra for loop
                JSONObject date_rooms = new JSONObject();
                date_rooms.put("date", date);
                JSONArray rooms = new JSONArray();
                for (Integer room : campus_data.get(date).keySet()) {
                    JSONObject room_records = new JSONObject();
                    room_records.put("room", room);
                    JSONArray records = new JSONArray();
                    for (RoomRecordClass roomRecordClass : campus_data.get(date).get(room)) {
                        JSONObject recordObj = new JSONObject();
                        recordObj.put("time_slot", roomRecordClass.time_slot);
                        recordObj.put("booked_by", roomRecordClass.booked_by);
                        recordObj.put("booking_id", roomRecordClass.booking_id);
                        records.put(recordObj);
                    }
                    room_records.put("records", records);
                    rooms.put(room_records);
                }
                date_rooms.put("rooms", rooms);
                data.put(date_rooms);
            }
            campusData.put("data", data);
            data_array.put(campusData);
        }

        JSONArray student_booking_array = new JSONArray();
        for (String student_id : student_booking.keySet()) {
            JSONObject student_booking_obj = new JSONObject();
            student_booking_obj.put("student_id", student_id);
            student_booking_obj.put("booking_count", student_booking.get(student_id));
            student_booking_array.put(student_booking_obj);
        }
        database.put("room_records", data_array);
        database.put("student_booking", student_booking_array);
        database.put("expected_sequence_number", expected.get());
        return database.toString(4);
    }
}
