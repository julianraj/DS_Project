package app;

import app.admin.Admin;
import app.server.ServerHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Date;
import java.util.Scanner;

public class AdminClient {

    private static Admin adminObject;
    private static String adminID;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter Admin ID: ");
            adminID = scanner.next();

            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            adminObject = ServerHelper.narrow(ncRef.resolve_str("frontend"));

            System.out.println("What do you want to do?");
            System.out.println("1: Create a room.");
            System.out.println("2: Delete a room.");
            System.out.println("");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            if (choice == 1 || choice == 2) {
                askForInput(scanner, choice);
            } else {
                System.out.println("Not a valid choice...");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void askForInput(Scanner scanner, int choice) {
        System.out.print("Enter room number: ");
        int roomNumber = scanner.nextInt();

        System.out.print("Enter date (dd-mm-yyyy): ");
        String date = scanner.next();

        System.out.print("Enter time slots as CSV (07:00-09:00,12:00-15:00): ");
        String timeSlots = scanner.next();
//        String[] availableTimeSlots = timeSlots.split(",");

        String logMessage, response = "--";

        switch (choice) {
            case 1:
                response = adminObject.createRoom(adminID, roomNumber, date, timeSlots);

                logMessage = "Date: " + new Date().toString();
                logMessage += "\nRequest Type: Create Room";
                logMessage += "\nParameters:";
                logMessage += "\n\tRoom number: " + roomNumber;
                logMessage += "\n\tDate: " + date;
                logMessage += "\n\tTime slots: " + timeSlots;
                logMessage += "\nServer response: " + response;
                Util.writeLog(adminID + ".log", logMessage);
                break;
            case 2:
                response = adminObject.deleteRoom(adminID, roomNumber, date, timeSlots);

                logMessage = "Date: " + new Date().toString();
                logMessage += "\nRequest Type: Delete Room";
                logMessage += "\nParameters:";
                logMessage += "\n\tRoom number: " + roomNumber;
                logMessage += "\n\tDate: " + date;
                logMessage += "\n\tTime slots: " + timeSlots;
                logMessage += "\nServer response: " + response;
                Util.writeLog(adminID + ".log", logMessage);
                break;
        }

        System.out.println(response);
    }
}
