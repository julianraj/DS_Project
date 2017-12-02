package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;





public final class RoomRecord
{
	public RoomRecord() {}

	public static int uniqueBookingID = 0;
	public static String serverName = "";
	public static HashMap<String, Integer> studentBookingCounter = new HashMap<>();
	public static HashMap<String, HashMap<String, ArrayList<Slot>>> dataBase = new HashMap<String, HashMap<String, ArrayList<Slot>>>();
	private static ReadWriteLock rwl = new ReentrantReadWriteLock();
	private static Lock readLock = rwl.readLock();
	private static Lock writeLock = rwl.writeLock();

	public static boolean hasDate(String date) {
		readLock.lock();
		try {
			if (dataBase.containsKey(date)) {
				return true;
			}
		} finally {
			readLock.unlock();
		}
		return false;
	}


	public static boolean hasRoom(String date, String roomNumber)
	{
		readLock.lock();
		try {
			if (dataBase.get(date).containsKey(roomNumber)) {
				return true;
			}
		} finally {
			readLock.unlock();
		}
		return false;
	}

	public static void createSchedule(String roomNumber, String date, String[] list_Of_Time_Slots)
	{
		try {
		ArrayList<Slot> slots = new ArrayList<Slot>();
		for (String s : list_Of_Time_Slots) {
			slots.add(new Slot(s));
		}
		writeLock.lock();
		HashMap<String, ArrayList<Slot>> put = new HashMap<String, ArrayList<Slot>>();
		put.put(roomNumber, slots);
		dataBase.put(date, put);
		} finally {
			writeLock.unlock();
		}
	}

	public static void addtimeSlotsToARoom(String roomNumber, String date, String[] list_Of_Time_Slots) {
	    try {	
			ArrayList<Slot> slots = new ArrayList<Slot>();
			for (String s: list_Of_Time_Slots) { 
				slots.add(new Slot(s));
			}
			writeLock.lock();
			dataBase.get(date).get(roomNumber).addAll(slots);
		} finally {
			writeLock.unlock();
		}
	}

	public static void createRoomAndInsertTimeSlots(String roomNumber, String date, String[] list_Of_Time_Slots)
	{
		ArrayList<Slot> slots = new ArrayList<Slot>();
		for(String s: list_Of_Time_Slots){ 
			slots.add(new Slot(s));
		}
		
		try {
			writeLock.lock();
			dataBase.get(date).put(roomNumber, slots);
		} finally {
			writeLock.unlock();
		}
	}


	public static boolean deleteSlots(String date, String roomNumber, String[] list_Of_Time_Slots) {
		writeLock.lock();
		boolean check = false;
		try{	
			ArrayList<Slot> slots = dataBase.get(date).get(roomNumber);
			for(String s: list_Of_Time_Slots){
				for(ListIterator<Slot> i = slots.listIterator(); i.hasNext();) {
					Slot slot = i.next();
					if(slot.getTimeSlot().equals(s)){
						i.remove();
						check = true;
					}
				}
			}
		}finally{
			writeLock.unlock();
		}
		return check;
	}

		
	
	public static String getAvailableTimeSlot(String date) {
		int n_availableSlots = 0;
		HashMap<String, ArrayList<Slot>> availableRooms = new HashMap<String, ArrayList<Slot>>();
		try{
		readLock.lock();
		
			if(dataBase.containsKey(date)){
				availableRooms = dataBase.get(date);
			}else{
				return serverName.toUpperCase()+" 0";
			}
				
		
		for(String key: availableRooms.keySet()){
		   ArrayList<Slot> slots = availableRooms.get(key);
		   for(Slot slot: slots){
			   if(!slot.isBooked())
				   n_availableSlots++;			   
		   }		   
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			readLock.unlock();
		}
		return serverName.toUpperCase()+" "+n_availableSlots;
	}


	public static boolean hasSlot(String roomNumber, String date, String timeslot) {
		
		if(!hasDate(date)){
			return false;
		}
		if(!hasRoom(date, roomNumber)){
			return false;
		}
		
		try{
			readLock.lock();
			ArrayList<Slot>  slots =  dataBase.get(date).get(roomNumber);
		    for(Slot s: slots){
		    	if(s.getTimeSlot().equals(timeslot)){		    	
		    		if(!s.isBooked())
		    			return true;
		    		break;
		    	}
		    }
		}finally{
			readLock.unlock();	
		}
		return false;
	}


	public static String bookSlot(String studentID,String roomNumber, String date, String timeslot) {
				
		if(!RoomRecord.hasSlot(roomNumber, date, timeslot)){
			return "fail";           
		}		
		
		try{
		    writeLock.lock();
		    ArrayList<Slot>  slots = dataBase.get(date).get(roomNumber);
			for(Slot s: slots){
		    	if(s.getTimeSlot().equals(timeslot)){
		    		if(!s.isBooked()){
		    			s.setBooked(true);
		    			s.setBookedBy(studentID);
		    			uniqueBookingID++;
		    			s.setBookingID(serverName+"_"+date+"_"+roomNumber+"_"+uniqueBookingID);
		    			return serverName+"_"+date+"_"+roomNumber+"_"+uniqueBookingID;
		    		}	
		    		break;
		    	}
		    }
		}finally{		
			writeLock.unlock();
		}
		return "fail";
	}


	public static int checkLimit(String studentID) {
		    try{
			readLock.lock();
		    Integer no_of_bookings = studentBookingCounter.get(studentID);
		    if(no_of_bookings != null){
            	return no_of_bookings;
            }else{
            	return 0;
            }
		    }finally{
		    	readLock.unlock();
		    }
	}


	public static String cancelBooking(String bookingID,String studentID){
		String[] b = bookingID.split("_");
		if(b.length != 4){
			return "fail";
		}
		boolean isSuccess = RoomRecord.isValidBooking(studentID, bookingID);
		if(!isSuccess){
			return "fail";
		}
		try{
			writeLock.lock();
			ArrayList<Slot> slots = null;
			slots = dataBase.get(b[1]).get(b[2]);
			for(Slot s: slots){
				if(s.getBookingID().equals(bookingID) && s.getBookedBy().equals(studentID)){
					s.setBooked(false);
					return "done";
				}
			}
		}catch(Exception e){
			return "fail";
		}finally{
			writeLock.unlock();
		}
		return "fail";
	}


	public static boolean isValidBooking(String studentID, String bookingID) {
		
		String[] b = bookingID.split("_");
		if(b.length != 4 || !hasDate(b[1]) || !hasRoom(b[1], b[2])){
			return false;
		}			
		try{
			readLock.lock();
			ArrayList<Slot> slots = null;
			slots = dataBase.get(b[1]).get(b[2]);
			for(Slot s: slots){
				if(s.isBooked() && s.getBookingID().equals(bookingID) && s.getBookedBy().equals(studentID)){
					return true;
				}
			}			
		}catch(Exception e){
			return false;
		}
		finally{
		   readLock.unlock();	
		}
		return false;
	}
}