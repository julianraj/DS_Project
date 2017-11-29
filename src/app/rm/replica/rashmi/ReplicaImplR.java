package app.rm.replica.rashmi;

import app.rm.replica.Replica;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReplicaImplR extends Replica<Server> {
    public static HashMap<String, HashMap<Integer, List<RoomRecordClass>>> KKL_data = new HashMap<>();
    public static HashMap<String, HashMap<Integer, List<RoomRecordClass>>> WST_data = new HashMap<>();
    public static HashMap<String, HashMap<Integer, List<RoomRecordClass>>> DVL_data = new HashMap<>();
    public static HashMap<String, Integer> student_booking = new HashMap<>();
    public static HashMap<Integer, String[]> queue;
    public static int expected = 1;
    private static Timer timer = new Timer(true);

    public ReplicaImplR(boolean hasError) {
        super(hasError);
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
                server = new Server(KKL_data, student_booking, campus, queue, expected);
            } else if (campus.equals("DVL")) {
                server = new Server(DVL_data, student_booking, campus,queue ,expected );
            } else {
                server = new Server(WST_data, student_booking, campus,queue ,expected );
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
            }
            campusData.put("data", data);
            data_array.put(campusData);
        }

        JSONArray student_booking_array = new JSONArray();
        for (String student_id : student_booking.keySet()) {
            JSONObject student_booking_obj = new JSONObject();
            student_booking_obj.put("student_id", student_id);
            student_booking_obj.put("booking_count",student_booking.get(student_id));
            student_booking_array.put(student_booking_obj);
        }
        database.put("room_records", data_array);
        return database.toString();
    }

    @Override
    public boolean ping(String campus) {
        return false;
    }


}
