/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.rm.replica.mudra;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class Record implements Serializable {
	  public String id;
	    public String bookedBy;
	    private static ReadWriteLock rwl = new ReentrantReadWriteLock();
	    private static Lock readLock = rwl.readLock();
	    public static HashMap<String, Integer> studentBookingCounter = new HashMap<>();

	    public Record(String id, String bookedBy) {
	        this.id = id;
	        this.bookedBy = bookedBy;
	    }

	    public Record() {
			// TODO Auto-generated constructor stub
		}

		public boolean patternMatcher(String s, String compile) {
	        Pattern p = Pattern.compile(compile);
	        Matcher m = p.matcher(s);
	        return m.matches();
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
}