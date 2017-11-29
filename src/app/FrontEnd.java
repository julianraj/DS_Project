package app;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;

public class FrontEnd {

    public static void main(String[] args) {

        try {
            ORB orb = ORB.init(args, null);

            POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPoa.the_POAManager().activate();

            org.omg.CORBA.Object objectRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectRef);

            FrontEndImpl frontEndImpl = new FrontEndImpl();
            frontEndImpl.setOrb(orb);

            org.omg.CORBA.Object ref = rootPoa.servant_to_reference(frontEndImpl);
            app.server.Server serverRef = app.server.ServerHelper.narrow(ref);

            NameComponent path[] = ncRef.to_name("frontend");
            ncRef.rebind(path, serverRef);

            System.out.println("Frontend is running...");

            /*new Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            DatagramSocket socket = null;
                            try {
                                socket = new DatagramSocket();
                                String message = "error:not-available";
                                byte[] data = message.getBytes();
                                InetAddress host = InetAddress.getByName("localhost");
                                DatagramPacket request = new DatagramPacket(data, data.length, host, Util.REPLICA_MANAGER_PORT);
                                socket.send(request);
                            } catch (IOException e) {
                            }
                        }
                    }, 0, 12000);*/


            orb.run();
        } catch (
                Exception e)

        {
            e.printStackTrace();
        }

        System.out.println("FrontEnd stopped...");
    }
}
