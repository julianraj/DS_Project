package app.rm.replica.rashmi;

import java.util.Random;

public class RoomRecordClass
{
    public String time_slot;
    public String booked_by;
    public String room_record_id;
    public String booking_id;

    public RoomRecordClass(String time_slot, String booked_by, String booking_id)
    {
        this.room_record_id = "RR" + String.format("%04d", new Random().nextInt(10000));
        this.time_slot = time_slot;
        this.booked_by = booked_by;
        this.booking_id = booking_id;
    }

}
