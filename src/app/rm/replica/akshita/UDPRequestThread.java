package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import model.RoomRecord;

public class UDPRequestThread implements Runnable{
	
	
	DatagramSocket socket = null;
    DatagramPacket request = null;

    public UDPRequestThread(DatagramSocket socket, DatagramPacket packet) {
        this.socket = socket;
        this.request = packet;
    }

	
	@Override
	public void run() {
		String tmp = new String(request.getData(),request.getOffset(),request.getLength()).trim();
		String[] query = tmp.split("-=",2);
		String response = null;
		System.out.println(tmp);
		String string = query[0];
		if ("getAvailabeTimeSlots".equals(string)) {
			response = RoomRecord.getAvailableTimeSlot(query[1]);
		} else if ("book".equals(string)) {
			String params = query[1];
			response = RoomRecord.bookSlot(getParameter(params, "ID"), getParameter(params, "ROOMNUMBER"), getParameter(params, "DATE"), getParameter(params, "SLOT"));
		} else if ("bookSlotAtomic".equals(string)) {
			String params1 = query[1];
			String bookingID = getParameter(params1, "BOOKINGID");
			String studentID = getParameter(params1, "ID");
			
			boolean issuccess = true;
			if(getParameter(params1, "CHECK").equals("yes")){
				issuccess =  RoomRecord.isValidBooking(studentID,bookingID);
			}
			if(!issuccess){
			    	response =  "fail";
			   }else{ 	
				   response = RoomRecord.bookSlot(studentID, getParameter(params1, "ROOMNUMBER"), getParameter(params1, "DATE"), getParameter(params1, "SLOT"));
				   if(!response.equals("fail") || !response.equals("overbooked")){
					  if(bookingID.split("_")[0].equals(RoomRecord.serverName)){
						  RoomRecord.cancelBooking(bookingID, studentID);
					  } 
				   }
			   }
		} else if ("cancel".equals(string)) {
			String params2 = query[1];
			String bookingId = getParameter(params2, "BOOKINGID");
			String studentId = getParameter(params2, "ID");
			response = RoomRecord.cancelBooking(bookingId, studentId);
		}
		else if ("checkValidBooking".equals(string)) {
			String params1 = query[1];
			String bookingID = getParameter(params1, "BOOKINGID");
			String studentID = getParameter(params1, "ID");
			response  = "success";
			if(!RoomRecord.isValidBooking(studentID,bookingID)){
				response = "fail";
			}
		}	
		else {
		}
		System.out.println("response "+response);
		byte[] b = response.getBytes();
		DatagramPacket reply = new DatagramPacket(b, b.length, 
				request.getAddress(), request.getPort());
		try {			
			socket.send(reply);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String getParameter(String str,String parameterName){
		str = str.substring(str.indexOf(parameterName) + parameterName.length()+1);
		System.out.println(str.substring(0, str.indexOf("-=")));
		return str.substring(0, str.indexOf("-="));
	}
	
}
