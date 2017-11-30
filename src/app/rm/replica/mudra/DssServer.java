package app.rm.replica.mudra;

import java.util.logging.Level;
import java.util.logging.Logger;


public class DssServer {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {


            // create servant instance and register it with the ORB
            ServerImpl KKLServer = new ServerImpl(ServerImpl.ServerDetails.KKL);
            ServerImpl DVLServer = new ServerImpl(ServerImpl.ServerDetails.DVL);
            ServerImpl WSTServer = new ServerImpl(ServerImpl.ServerDetails.WST);

        } catch (Exception ex) {
            Logger.getLogger(DssServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}