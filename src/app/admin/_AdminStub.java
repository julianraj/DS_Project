package app.admin;


/**
* app/admin/_AdminStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from App.idl
* Tuesday, November 28, 2017 11:34:48 PM EST
*/

public class _AdminStub extends org.omg.CORBA.portable.ObjectImpl implements app.admin.Admin
{

  public String createRoom (String adminID, int roomNumber, String date, String timeSlots)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createRoom", true);
                $out.write_string (adminID);
                $out.write_long (roomNumber);
                $out.write_string (date);
                $out.write_string (timeSlots);
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

  public String deleteRoom (String adminID, int roomNumber, String date, String timeSlots)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("deleteRoom", true);
                $out.write_string (adminID);
                $out.write_long (roomNumber);
                $out.write_string (date);
                $out.write_string (timeSlots);
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

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:app/admin/Admin:1.0"};

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
} // class _AdminStub
