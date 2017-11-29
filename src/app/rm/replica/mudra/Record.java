package app.rm.replica.mudra;

public class Record {

    String bookingId;
    String bookedBy;

    public Record(String bookingId, String bookedBy) {
        this.bookingId = bookingId;
        this.bookedBy = bookedBy;
    }

    public Record() {
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }
}
