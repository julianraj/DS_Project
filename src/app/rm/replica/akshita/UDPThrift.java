package app.rm.replica.akshita;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class UDPThrift {

    private static UDPThrift singletonUDP = null;

    static ExecutorService threadPool = null;
	private UDPThrift(){
		threadPool = Executors.newCachedThreadPool();
	}
	
	 public static void init(int port) {
		    if (singletonUDP == null) {
	            synchronized (UDPThrift.class) {
	                if (singletonUDP == null) { 
	                	singletonUDP = new UDPThrift();
	                	System.out.println("Started UDP Server too");		                
	                	singletonUDP.startUDPServer(port);
	                }
	            }
	        }
	  }
	
	 
	 public void startUDPServer(int port){
		 
		 DatagramSocket aSocket = null;
		 try{
		    aSocket = new DatagramSocket(port);
			while(true){
                byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
	  			aSocket.receive(request);
	  			//queue
	  			threadPool.execute( new UDPRequestThread(aSocket, request));  
			  }
		   }catch (SocketException e){ 
			   e.printStackTrace(); 
		   
		   }catch (IOException e) {  
			   e.printStackTrace(); 
		   
		   }
		   finally {
			   System.out.println("Socket closed at port " + port);
			   if(aSocket != null) aSocket.close();
			  
		   }
	 
	 }	
}
