package app;

import app.server.ServerHelper;
import app.student.Student;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Date;
import java.util.Scanner;

public class StudentClient {

    private static Student studentObject;
    private static String studentID;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter Student ID: ");
            studentID = scanner.next();

            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            studentObject = ServerHelper.narrow(ncRef.resolve_str("frontend"));

            System.out.println("What do you want to do?");
            System.out.println("1: Book a room.");
            System.out.println("2: Get available time slots");
            System.out.println("3: Cancel booking.");
            System.out.println("4: Change reservation.");
            System.out.println("");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            String logMessage, response = "--";

            switch (choice) {
                case 1:
                    System.out.print("Enter Campus name: ");
                    String campusName = scanner.next();

                    System.out.print("Enter room number: ");
                    int roomNumber = scanner.nextInt();

                    System.out.print("Enter date (dd-mm-yyyy): ");
                    String date = scanner.next();

                    System.out.print("Enter time-slot: ");
                    String timeSlot = scanner.next();

                    response = studentObject.bookRoom(studentID, campusName, roomNumber, date, timeSlot);
                    response = response.equals("error") ? "Could not perform your request." : response;

                    logMessage = "Date: " + new Date().toString();
                    logMessage += "\nRequest Type: Book Room";
                    logMessage += "\nParameters:";
                    logMessage += "\n\tCampus name: " + campusName;
                    logMessage += "\n\tRoom number: " + roomNumber;
                    logMessage += "\n\tDate: " + date;
                    logMessage += "\n\tTime slot: " + timeSlot;
                    logMessage += "\nServer response: " + response;
                    Util.writeLog(studentID + ".log", logMessage);

                    break;

                case 2:
                    System.out.print("Enter date (dd-mm-yyyy): ");
                    String lookUpDate = scanner.next();

                    response = studentObject.getAvailableTimeSlots(studentID, lookUpDate);

                    logMessage = "Date: " + new Date().toString();
                    logMessage += "\nRequest Type: Get available time slots";
                    logMessage += "\nParameters:";
                    logMessage += "\n\tDate: " + lookUpDate;
                    logMessage += "\nServer response: " + response;
                    Util.writeLog(studentID + ".log", logMessage);
                    break;

                case 3:
                    System.out.print("Enter BookingID: ");
                    String bookingID = scanner.next();

                    response = studentObject.cancelBooking(studentID, bookingID);
                    response = response.equals("error") ? "Could not perform your request." : response;

                    logMessage = "Date: " + new Date().toString();
                    logMessage += "\nRequest Type: Cancel Booking";
                    logMessage += "\nParameters:";
                    logMessage += "\n\tBooking ID: " + bookingID;
                    logMessage += "\nServer response: " + response;
                    Util.writeLog(studentID + ".log", logMessage);
                    break;

                case 4:
                    System.out.print("Enter previous BookingID: ");
                    String oldBookingID = scanner.next();

                    System.out.print("Enter new Campus name: ");
                    String newCampusName = scanner.next();

                    System.out.print("Enter new room number: ");
                    int newRoomNumber = scanner.nextInt();

                    System.out.print("Enter new date (dd-mm-yyyy): ");
                    String newDate = scanner.next();

                    System.out.print("Enter new time-slot: ");
                    String newTimeSlot = scanner.next();

                    response = studentObject.changeReservation(studentID, oldBookingID, newCampusName, newRoomNumber, newDate, newTimeSlot);

                    logMessage = "Date: " + new Date().toString();
                    logMessage += "\nRequest Type: Change Reservation";
                    logMessage += "\nParameters:";
                    logMessage += "\n\tprevious Booking ID: " + oldBookingID;
                    logMessage += "\n\tnew Campus name: " + newCampusName;
                    logMessage += "\n\tnew Room number: " + newRoomNumber;
                    logMessage += "\n\tnew Date: " + newDate;
                    logMessage += "\n\tnew Time slot: " + newTimeSlot;
                    logMessage += "\nServer response: " + response;
                    Util.writeLog(studentID + ".log", logMessage);

                    break;
            }
            System.out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }
}
