package app.server;


/**
* app/server/_ServerStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from App.idl
* Wednesday, November 8, 2017 12:33:45 AM EST
*/

public class _ServerStub extends org.omg.CORBA.portable.ObjectImpl implements app.server.Server
{

  public String createRoom (String adminID, int roomNumber, String date, String[] timeSlots)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createRoom", true);
                $out.write_string (adminID);
                $out.write_long (roomNumber);
                $out.write_string (date);
                app.admin.AdminPackage.timeSlotSequenceHelper.write ($out, timeSlots);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return createRoom (adminID, roomNumber, date, timeSlots        );
            } finally {
                _releaseReply ($in);
            }
  } // createRoom

  public String deleteRoom (String adminID, int roomNumber, String date, String[] timeSlots)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("deleteRoom", true);
                $out.write_string (adminID);
                $out.write_long (roomNumber);
                $out.write_string (date);
                app.admin.AdminPackage.timeSlotSequenceHelper.write ($out, timeSlots);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return deleteRoom (adminID, roomNumber, date, timeSlots        );
            } finally {
                _releaseReply ($in);
            }
  } // deleteRoom

  public String bookRoom (String studentID, String campusName, int roomNumber, String date, String timeSlot)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("bookRoom", true);
                $out.write_string (studentID);
                $out.write_string (campusName);
                $out.write_long (roomNumber);
                $out.write_string (date);
                $out.write_string (timeSlot);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return bookRoom (studentID, campusName, roomNumber, date, timeSlot        );
            } finally {
                _releaseReply ($in);
            }
  } // bookRoom

  public String getAvailableTimeSlots (String studentID, String date)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("getAvailableTimeSlots", true);
                $out.write_string (studentID);
                $out.write_string (date);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return getAvailableTimeSlots (studentID, date        );
            } finally {
                _releaseReply ($in);
            }
  } // getAvailableTimeSlots

  public String cancelBooking (String studentID, String bookingID)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("cancelBooking", true);
                $out.write_string (studentID);
                $out.write_string (bookingID);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return cancelBooking (studentID, bookingID        );
            } finally {
                _releaseReply ($in);
            }
  } // cancelBooking

  public String changeReservation (String studentID, String bookingID, String campusName, int roomNumber, String date, String timesSlot)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("changeReservation", true);
                $out.write_string (studentID);
                $out.write_string (bookingID);
                $out.write_string (campusName);
                $out.write_long (roomNumber);
                $out.write_string (date);
                $out.write_string (timesSlot);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return changeReservation (studentID, bookingID, campusName, roomNumber, date, timesSlot        );
            } finally {
                _releaseReply ($in);
            }
  } // changeReservation

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:app/server/Server:1.0", 
    "IDL:app/admin/Admin:1.0", 
    "IDL:app/student/Student:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _ServerStub