package app.rm.replica.rashmi;


/**
* app/CampusOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from app.idl
* Monday, November 27, 2017 1:17:22 o'clock PM EST
*/

public interface CampusOperations 
{
  String createRoom (String admin_id, int room_number, String date, String[] list_of_time_slots);
  String deleteRoom (String admin_id, int room_number, String date, String[] list_of_time_slots);
  String bookRoom (String student_id, String campus_name, int room_number, String date, String slots);
  String getTimeSlotsCampus (String date);
  String cancelBooking (String booking_id, String student_id);
  String changeReservation (String booking_id, String new_campus_name, int new_room_no, String new_date, String new_time_slot, String student_id);
} // interface CampusOperations
