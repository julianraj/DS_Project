package app.rm.replica.akshita;



public class AdminImpl {
	
	private static final long serialVersionUID = 1L;

	FileLogger fileLogger = null;
	public AdminImpl(String serverName){
		fileLogger = new FileLogger(serverName);		
	}

	public boolean resetCount() {
		RoomRecord.studentBookingCounter.clear();
		
		return false;
	}

	public boolean createRoom(String roomNumber, String date, String[] list_Of_Time_Slots) {
		if(RoomRecord.hasDate(date)) {
			if (RoomRecord.hasRoom(date, roomNumber)) {
				RoomRecord.addtimeSlotsToARoom(roomNumber, date, list_Of_Time_Slots);
			} else {
				RoomRecord.createRoomAndInsertTimeSlots(roomNumber, date, list_Of_Time_Slots);
			}
			fileLogger.writeLog("Added time slot to "+date+" at "+roomNumber+" with ",list_Of_Time_Slots);
				
		} else {
			RoomRecord.createSchedule(roomNumber, date, list_Of_Time_Slots);
			fileLogger.writeLog("Created time slot on "+date+" at "+roomNumber+" with ",list_Of_Time_Slots);
			
		}

		return true;
		
	}

	public boolean deleteRoom(String roomNumber, String date, String[] list_Of_Time_Slots) {
		if (RoomRecord.hasDate(date)) {
			if(RoomRecord.hasRoom(date, roomNumber)){
				boolean done = RoomRecord.deleteSlots(date,roomNumber,list_Of_Time_Slots);
				if(done){
				  fileLogger.writeLog("Deleted slot on "+date+" at "+roomNumber,list_Of_Time_Slots);					
				  return true;
				}else{
					fileLogger.writeLog("Failed to delete on "+date+" at "+roomNumber+" as not room exist");					
				    return false;
				} 
			} else {
				fileLogger.writeLog("Failed to delete on "+date+" at "+roomNumber+" as not room exist");					
			    return false;
			}
		}else{
			fileLogger.writeLog("Failed to delete on "+date+" at "+roomNumber+" as not date exist");
			return false;
		}		
	}
}
