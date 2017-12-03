package app.rm.replica.akshita;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class FileLogger {

	String filePath = null;
	BufferedWriter writer = null;

	public FileLogger(String user){
		File f = new File((filePath = "C:\\Users\\akshi\\workspace\\Assignment2\\ServerLog"+user+".txt"));
        	try {
				f.createNewFile();
				writer = new BufferedWriter(new FileWriter(f, true));
				writer.write("----------------------------------------------------------");
				writer.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
	}
	
	public void writeLog(String value){	        	
		try {
			writer.write(new Date()+": "+value);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void writeLog(String value, String[] slots){
	        	
		   String tmp = "";
		   for(String s: slots){
			   tmp += ", "+s;
		   }
		   try {
			writer.write(new Date()+": "+value+" "+tmp);
			writer.newLine();
			writer.flush();
		   } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void closeWriter(){
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
