package app.admin.AdminPackage;


/**
* app/admin/AdminPackage/timeSlotSequenceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from App.idl
* Wednesday, November 8, 2017 12:33:45 AM EST
*/

public final class timeSlotSequenceHolder implements org.omg.CORBA.portable.Streamable
{
  public String value[] = null;

  public timeSlotSequenceHolder ()
  {
  }

  public timeSlotSequenceHolder (String[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = app.admin.AdminPackage.timeSlotSequenceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    app.admin.AdminPackage.timeSlotSequenceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return app.admin.AdminPackage.timeSlotSequenceHelper.type ();
  }

}