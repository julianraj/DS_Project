package model;

public class Slot {
  private String timeSlot;
  private boolean isBooked = false;
  private String bookedBy;
  private String bookingID;
  
  public Slot(String timeSlot) {
    this.timeSlot = timeSlot;
  }
  
  public String getTimeSlot() {
    return timeSlot;
  }
  
  public void setTimeSlot(String timeSlot) {
    this.timeSlot = timeSlot;
  }
  
  public boolean isBooked() {
    return isBooked;
  }
  
  public void setBooked(boolean isBooked) {
    this.isBooked = isBooked;
  }
  
  public String getBookedBy() {
    return bookedBy;
  }
  
  public void setBookedBy(String bookedBy) {
    this.bookedBy = bookedBy;
  }

public String getBookingID() {
	return bookingID;
}

public void setBookingID(String bookingID) {
	this.bookingID = bookingID;
}
}