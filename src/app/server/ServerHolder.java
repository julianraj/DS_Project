package app.server;

/**
* app/server/ServerHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from App.idl
* Tuesday, November 28, 2017 11:34:48 PM EST
*/

public final class ServerHolder implements org.omg.CORBA.portable.Streamable
{
  public app.server.Server value = null;

  public ServerHolder ()
  {
  }

  public ServerHolder (app.server.Server initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = app.server.ServerHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    app.server.ServerHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return app.server.ServerHelper.type ();
  }

}
