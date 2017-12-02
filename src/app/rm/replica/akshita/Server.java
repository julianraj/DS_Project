package server;

import admin.AdminImpl;
import admin.AdminInterface;
import admin.AdminInterfaceHelper;
import student.StudentImpl;
import student.StudentImpl.ServerData;
import student.StudentInterface;
import student.StudentInterfaceHelper;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.Scanner;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;



public class Server
{

	public Server() {
	}
	
	private static POA rootpoa;
	private static ORB orb;
	private static NamingContextExt ncRef;
	private static AdminImpl adminImpl;
	private static StudentImpl studentImpl;
	private static ServerData s = null;

	private static void bind() throws ServantNotActive, WrongPolicy, InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound, CannotProceed {
		
		adminImpl = new AdminImpl(s.tag);
        studentImpl = new StudentImpl(s);
        
		/*
         * A CORBA object reference is a handle for a particular CORBA object implemented by a server. 
         * A CORBA object reference identifies the same CORBA object each time the reference is used to invoke a method on the object. A CORBA object may have multiple, distinct object references. 
         */
        
        // get naming service
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        ncRef = NamingContextExtHelper.narrow(objRef);
        
		//bind server
		bindServer();
		
    	new Thread(new Runnable() {			
			@Override
			public void run() {
				UDPThrift.init(s.udpListenerPort);
			}
		}).start();
	}
	
	public static void bindServer() throws ServantNotActive, WrongPolicy, InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound, CannotProceed {
		org.omg.CORBA.Object ref = rootpoa.servant_to_reference(adminImpl);
        AdminInterface href = AdminInterfaceHelper.narrow(ref);
        NameComponent path[] = ncRef.to_name( s.tag.toLowerCase()  + "_admin_service" );
        ncRef.rebind(path, ref);
        
        org.omg.CORBA.Object ref1 = rootpoa.servant_to_reference(studentImpl);
        StudentInterface href1 = StudentInterfaceHelper.narrow(ref1);
        NameComponent path1[] = ncRef.to_name( s.tag.toLowerCase() + "_student_service" );
        ncRef.rebind(path1, ref1);
        
        System.out.println("Server Started at " + s.tag + " Campus");
	}
	
	public static void unbindServer() throws ServantNotActive, WrongPolicy, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound, CannotProceed {
		NameComponent path[] = ncRef.to_name( s.tag.toLowerCase()  + "_admin_service" );
        ncRef.unbind(path);
        
        NameComponent path1[] = ncRef.to_name( s.tag.toLowerCase() + "_student_service" );
        ncRef.unbind(path1);
        
        // CORBA server stopped
        System.out.println("Server Stopped at " + s.tag + " Campus");
	}
  
  public static void main(String[] args)
  {
    try
    {
        orb = ORB.init(args, null);
        
        rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        rootpoa.the_POAManager().activate();

        System.out.print("Enter server Name : ");
        Scanner sc = new Scanner(System.in);
        s = getServer(sc.nextLine());
        if(s!=null) {
        	bind();
        	orb.run();
        }
    }
   catch(Exception e){
    	e.printStackTrace();
    }
  }
  
  public static ServerData getServer(String tag) {
	  for(ServerData s : ServerData.values()) {
		  if(s.tag.equals(tag.toUpperCase()))
			  return s;
	  }
	  return null;
  }
}