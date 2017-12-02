package app.rm.replica.akshita;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;




public class StudentImpl{

	private static final long serialVersionUID = -3296328504325261792L;
	DatagramSocket aSocket = null;
	InetAddress aHost;
	FileLogger fileLogger = null;
	ServerData currentServerData;

	public enum ServerData {
		KKL("Kirkland-Server", "KKL", 7001, 8001), DVL("Dorval-Server","DVL",7002, 8002), WST("Westmount-Server","WST", 7003, 8003);
		
		public String name, tag;
		public int udpListenerPort, udpSenderPort;
		public String bindName;
		
		private ServerData(String name, String tag, int udpListenerPort, int udpSenderPort) {
			this.name = name;
			this.tag = tag;
			this.udpListenerPort = udpListenerPort;
			this.udpSenderPort = udpSenderPort;
			this.bindName = tag.toLowerCase() + "_student_service";
		}
	}

	public String responseFromAllServers = "",bookingReply="",cancelReply="",isValidBooking="";
	
	public StudentImpl(ServerData serverData) {
		this.currentServerData = serverData;
		RoomRecord.serverName = serverData.tag;
		fileLogger = new FileLogger(serverData.tag);
		try {
			aHost = InetAddress.getByName("localhost");
			aSocket = new DatagramSocket(serverData.udpSenderPort);
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public ServerData[] getOtherServer() {
		ServerData[] serverDatas = ServerData.values();
		for(int i=0; i<serverDatas.length; i++)
			if(serverDatas[i].equals(currentServerData))
				serverDatas[i] = null;
		return serverDatas;
	}

	public String bookRoom(String campusName,String studentID, String roomNumber, String date, String timeslot){
		
		String tmp;
		int limit = RoomRecord.checkLimit(studentID);
		if(limit >= 3){
			return "overbooked";
		}
		
		if(RoomRecord.serverName.equals(campusName.toLowerCase())){
			tmp = RoomRecord.bookSlot(studentID,roomNumber, date, timeslot);
		    fileLogger.writeLog(studentID+" booked "+roomNumber+" on "+date+" with status "+tmp);
		}else{
			//do booking from other server
			bookingReply="";
			String message = "book-=ID="+studentID+"-=ROOMNUMBER="+roomNumber+"-=DATE="+date+"-=SLOT="+timeslot+"-=";
			contactOtherServers(message);			
		    tmp = bookingReply;
		}
		
		if(!tmp.equals("fail")){
			RoomRecord.studentBookingCounter.put(studentID, limit+1);
		}
		
		return tmp;
	}

	public String getAvailableTimeSlot(String date) {
		//get data from other servers
		responseFromAllServers = "";		 
		responseFromAllServers = RoomRecord.getAvailableTimeSlot(date);
		contactOtherServers("getAvailabeTimeSlots-="+date);
		return responseFromAllServers;
	}

	

	private boolean contactOtherServers(String query) {
	    //gather data from two other server
	    CountDownLatch latch = new CountDownLatch(ServerData.values().length-1);
	    for(ServerData s : getOtherServer())
			if(s!=null)
				new Thread(new getDataFromUDP(latch,s.udpListenerPort,query)).start();
		try {
			//wait until got response from all other servers 
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private void contactOtherServersAsync(String query) {			
		for(ServerData s : getOtherServer())
			if(s!=null)
				new Thread(new getDataFromUDP(null,s.udpListenerPort,query)).start();
	}
	
	public boolean cancelBooking(String bookingID,String studentID) {
		
		
		Integer tmp = RoomRecord.studentBookingCounter.get(studentID);
		if(tmp== null || (tmp-1) <0){
			return false;
		}
		
		String issuccess = null;
		String campusName = bookingID.split("_")[0];
		
		if(campusName.equals(RoomRecord.serverName)){
			issuccess = RoomRecord.cancelBooking(bookingID,studentID);
		}else{
			String message = "cancel-=ID="+studentID+"-=BOOKINGID="+bookingID+"-=";
    		contactOtherServers(message);
			issuccess = cancelReply;
		}
		
		
		if(issuccess.equals("done")){
			RoomRecord.studentBookingCounter.put(studentID,tmp-1);			
			fileLogger.writeLog("Canceled the booking "+bookingID);
		}else{
			fileLogger.writeLog("Failed to cancel the booking "+bookingID);
			return false;
		}
		return true;
	}

	
	
	public String changeReservation(String booking_id, String new_campus_name, String new_room_no,
			String new_time_slot,String studentID) {	    
	    
		 String date = booking_id.split("_")[1];
		    String previousCampus = booking_id.split("_")[0];
		    String tmp = null,doCheck = "no";
		    
		    if(previousCampus.equals(RoomRecord.serverName)){
		    	//check whether the booking id is real or not
		    	boolean issuccess = RoomRecord.isValidBooking(studentID,booking_id);
			    if(!issuccess)
			    	return "fail";
		    }else if(previousCampus.equals(new_campus_name)){
		    	doCheck = "yes";
		    }else{
		        //check with other server whether booking ID is valid
		    	contactOtherServers("checkValidBooking-=ID="+studentID+"-=BOOKINGID="+booking_id+"-=");
		    	System.err.println(isValidBooking);
		    	if(isValidBooking.equals("fail")){
		    		return "fail";
		    	}
		    }
	    //if new campus and present campus are same do booking here
	    if(new_campus_name.toLowerCase().equals(RoomRecord.serverName)){		    
	    	tmp = RoomRecord.bookSlot(studentID,new_room_no, date, new_time_slot); 
	    	    	
	    }else{
	    	bookingReply="";
	    	String message = "bookSlotAtomic-=ID="+studentID+"-=ROOMNUMBER="+new_room_no+"-=DATE="+date+"-=SLOT="+new_time_slot+"-=BOOKINGID="+booking_id+"-=CHECK="+doCheck+"-=";
			contactOtherServers(message);			
	        tmp = bookingReply;
	    }
	    
	    fileLogger.writeLog("Changed timeslot for "+booking_id+" to "+new_room_no+" with status/ID "+tmp);
	    
	    if(!tmp.equals("fail") && !tmp.equals("overbooked")){
	    	if(previousCampus.equals(RoomRecord.serverName)){
	    		RoomRecord.cancelBooking(booking_id, studentID);
	    	}else if(!new_campus_name.equals(previousCampus)){
		    	String message = "cancelBooking:ID="+studentID+";BOOKINGID="+booking_id+";";
				contactOtherServersAsync(message);
	    	}
	    }
	    
	    return tmp;
	}	
	
	
	
	
	class getDataFromUDP implements Runnable{

		CountDownLatch signal = null;
		String query = null;
		int port = 0;
		public getDataFromUDP(CountDownLatch latch,int n_port,String n_query){
			this.signal = latch;
			this.port = n_port;
			this.query = n_query;
		}

		@Override
		public void run() {
			try {    
				byte [] m = this.query.getBytes();
				DatagramPacket request = new DatagramPacket(m,  m.length, aHost, this.port);
				aSocket.send(request);			                        
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	
				aSocket.receive(reply);
				String s = new String(reply.getData(), reply.getOffset(),reply.getLength());			
				switch(query.split(":",2)[0]){
				   case "getAvailabeSlot":
					   synchronized(responseFromAllServers){
				           responseFromAllServers += ", "+s;
				        }
					   break;
				   case "bookSlot":
					   bookingReply=s;
					   break;
				   case "bookSlotAtomic":
					   bookingReply=s;
					   break;
				   case "cancelBooking":
					   cancelReply = s;
				   case "checkValidBooking":
					   isValidBooking = s;
				   default:
					   break;
				}
			if(signal != null)
			    signal.countDown();			    
			}catch (SocketException e){ e.printStackTrace();
			}catch (IOException e){ e.printStackTrace(); 
			}finally {}			
		}

	}
	
}
