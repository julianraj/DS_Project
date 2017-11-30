package app.rm.replica.mudra;

import app.rm.replica.Replica;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class ReplicaImplM extends Replica<ServerImpl> {

    private static HashMap<String, HashMap<String, HashMap<String, HashMap<String, Record>>>> mData = new HashMap<>();
    private static int expectedSequenceNumber = 1;

    public ReplicaImplM(int replicaIndex, boolean hasError) {
        super(replicaIndex, hasError);

    }

    @Override
    protected void start(boolean requestData) {
        if (mData == null) mData = new HashMap<>();
        if (requestData) requestData();

        try {
            for (String campus : campuses) {
                if (!requestData) mData.put(campus, new HashMap<>());

                ServerImpl.ServerDetails details;
                if (campus.equals("KKL")) details = ServerImpl.ServerDetails.KKL;
                else if (campus.equals("DVL")) details = ServerImpl.ServerDetails.DVL;
                else details = ServerImpl.ServerDetails.WST;
                ServerImpl server = new ServerImpl(details);
                server.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void restart() {

    }

    @Override
    protected void mapJsonToData(String json) {
        try {
            if (mData.isEmpty()) {
                JSONObject jsonData = new JSONObject(json);
                JSONArray database = jsonData.getJSONArray("room_records");
                JSONArray studentData = jsonData.getJSONArray("student_booking");

                for (int i = 0; i < database.length(); i++) {
                    JSONObject obj = database.getJSONObject(i);
                    JSONArray campusData = obj.getJSONArray("data");
                    HashMap<String, HashMap<String, HashMap<String, Record>>> data = new HashMap<>();

                    for (int j = 0; j < campusData.length(); j++) {
                        JSONObject campusObj = campusData.getJSONObject(j);
                        HashMap<String, HashMap<String, Record>> roomData = new HashMap<>();
                        JSONArray rooms = campusObj.getJSONArray("rooms");

                        for (int k = 0; k < rooms.length(); k++) {
                            JSONObject roomObj = rooms.getJSONObject(k);
                            JSONArray recordData = roomObj.getJSONArray("records");
                            HashMap<String, Record> records = new HashMap<>();

                            for (int l = 0; l < recordData.length(); l++) {
                                JSONObject recordObj = recordData.getJSONObject(i);

                                Record record = null;
                                if (recordObj.has("booked_by")) {
                                    record = new Record();
                                    record.bookedBy = recordObj.getString("booked_by");
                                    record.id = recordObj.getString("booking_id");
                                }
                                records.put(recordObj.getString("time_slot"), record);
                            }
                            roomData.put(roomObj.getString("room"), records);
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
                for (String room : mData.get(campus).get(date).keySet()) {
                    JSONObject roomData = new JSONObject();
                    roomData.put("room", room);

                    JSONArray roomDatabase = new JSONArray();
                    for (String timeslot : mData.get(campus).get(date).get(room).keySet()) {
                        JSONObject recordData = new JSONObject();
                        recordData.put("time_slot", timeslot);
                        recordData.put("booked_by", mData.get(campus).get(date).get(room).get(timeslot).bookedBy);
                        recordData.put("booking_id", mData.get(campus).get(date).get(room).get(timeslot).id);
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
        /*for (String student : mStudentData.keySet()) {
            JSONObject obj = new JSONObject();
            obj.put("student_id", student);
            obj.put("booking_count", mStudentData.get(student));
            bookingCountData.put(obj);
        }*/
        myData.put("student_booking", bookingCountData);
        myData.put("expected_sequence_number", expectedSequenceNumber);

        return myData.toString(4);
    }

    @Override
    public boolean ping(String campus) {
        return false;
    }
}
