package app.rm.replica.julian;

import java.util.List;
import java.util.Random;

public class RoomRecord {
    public String mRecordID;
    public String mTimeSlot;
    public String mBookedBy;
    public String mBookingID;

    public RoomRecord() {
        mRecordID = "RR" + String.format("%04d", new Random().nextInt(10000));
    }

    public RoomRecord(String timeSlot, String bookedBy) {
        this();
        mTimeSlot = timeSlot;
        mBookedBy = bookedBy;
    }

    public void setRecordID(String recordID) {
        mRecordID = recordID;
    }

    public void setTimeSlot(String timeSlot) {
        mTimeSlot = timeSlot;
    }

    public void setBookedBy(String bookedBy) {
        mBookedBy = bookedBy;
    }

    public void setBookingID(String bookingID) {
        mBookingID = bookingID;
    }

    public static boolean hasTimeSlot(List<RoomRecord> records, String timeSlot) {
        for (RoomRecord record : records) {
            if (record.mTimeSlot.equals(timeSlot)) {
                return true;
            }
        }
        return false;
    }


}
