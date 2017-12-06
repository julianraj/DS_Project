package app.client;

import app.Helper;
import app.server.Server;
import app.server.ServerHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Scanner;

public class AdminClient {
    static Server frontend;

    public static void main(String args[])
    {
        try
        {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter your ID : ");
            String id = scanner.nextLine();
            String campusName = id.substring(0, 3);
            System.out.println(campusName);

            ORB orb = ORB.init(args, null);
            Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            frontend = ServerHelper.narrow(ncRef.resolve_str("frontend"));


            System.out.println("What would you like to do today? \r\n " +
                    "1. Create a Room \r\n " +
                    "2. Delete a Room \r\n " +
                    "Press 1 or 2 to continue");
            int input = scanner.nextInt();

            System.out.println("Enter room number");
            int room_number = scanner.nextInt();

            System.out.println("Enter date ('mm-dd-yyyy')");
            String date = scanner.next();

            System.out.println("Enter your time slots separated by a comma ','");
            String list_of_time_slots = scanner.next();

//            String slots[] = list_of_time_slots.split(",");

            switch (input)
            {
                case 1:
                {
                    String message = frontend.createRoom(id, room_number, date, list_of_time_slots );
                    String parameters = "Room Number: " + room_number + " Date: " + date + " Slots: " + list_of_time_slots;
                    Helper.writelog("Client","Admin", id, "Create Room", parameters, true, "Room Created");//todo
                    System.out.println(message);

                }
                break;
                case 2:
                {
                    String message = frontend.deleteRoom(id, room_number, date, list_of_time_slots);
                    String parameters = "Room Number: " + room_number + " Date: " + date + " Slots: " + list_of_time_slots;
                    Helper.writelog("Client", "Admin",id, "Delete Room", parameters, true, message);//todo
                    System.out.println(message);
                }
                break;
                default:
                    System.out.println("your input is invalid!");
                    break;
            }


        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
