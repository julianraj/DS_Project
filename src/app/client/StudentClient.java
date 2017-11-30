package app.client;

import app.Helper;
import app.server.Server;
import app.server.ServerHelper;
import app.student.Student;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Scanner;

public class StudentClient {
    static Student frontend;
    public static void main(String args[])
    {
        try
        {


            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your student id");
            String student_id = scanner.next();
            String campus_name = student_id.substring(0, 3);
            System.out.println(campus_name);


            ORB orb = ORB.init(args, null);
            Object objRef = orb.resolve_initial_references("NameService");//locate registry jasto ho
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);//locate registry
            frontend = ServerHelper.narrow(ncRef.resolve_str("frontend"));//helper class le lookup gardincha narrow use garera


            System.out.println("What would you like to do today? \r\n " +
                    "1. Book a Room \r\n " +
                    "2. Get Available Time Slots \r\n " +
                    "3. Cancel a Booking \r\n " +
                    "4. Change Reservation \r\n "+
                    "Press 1, 2 or 3 to continue");
            int input = scanner.nextInt();

            switch (input)
            {
                case 1:
                {
                    System.out.println("Enter a campus you want to book a room in");
                    String book_campus_name = scanner.next();

                    System.out.println("Enter a room number you want to book");
                    int room_number = scanner.nextInt();

                    System.out.println("Enter the date you want to book in");
                    String date = scanner.next();

                    System.out.println("Enter your time slot");
                    String slots = scanner.next();

                    String message = frontend.bookRoom(student_id, book_campus_name, room_number, date, slots);
                    String parameters = "Student Id: " + student_id + "Campus Name: " + book_campus_name + " Room Number: " + room_number + " Date: " + date + " Slots: " + slots;
                    Helper.writelog("Client","Student", student_id, "Book Room", parameters, true, "Booking Id is " + message);//todo
                    System.out.println(message);
                }
                break;
                case 2:
                {
                    System.out.println("Enter the date you want to book in");
                    String date = scanner.next();
                    String message = frontend.getAvailableTimeSlots(student_id,date);
                    String parameters = " Date: " + date;
                    Helper.writelog("Client","Student", student_id, "Get Time SLots", parameters, true, message);//todo
                    System.out.println(message);

                }
                break;
                case 3:
                {
                    System.out.println("Enter a booking id");
                    String booking_id = scanner.next();
                    String message = frontend.cancelBooking(booking_id, student_id);
                    String parameters = "Student Id: " + student_id + " Booking ID: " + booking_id;
                    Helper.writelog("Client","student", student_id, "Cancel Room", parameters, true, "Room Booking Cancelled");//todo
                    System.out.println(message);
                }
                break;
                case 4:
                {
                    System.out.println("Enter a booking id");
                    String booking_id = scanner.next();
                    System.out.println("Enter a campus you want to book a room in");
                    String book_campus_name = scanner.next();

                    System.out.println("Enter a room number you want to book");
                    int room_number = scanner.nextInt();

                    System.out.println("Enter the date you want to book in");
                    String date = scanner.next();

                    System.out.println("Enter your time slot");
                    String slots = scanner.next();
                    String message = frontend.changeReservation(student_id,booking_id,book_campus_name,room_number,date, slots);
                    String parameters = "Student Id: " + student_id + " Booking ID: " + booking_id;
                    Helper.writelog("Client","Student", student_id, "Change Reservation", parameters, true, "Room Booking Cancelled. New booking created " + message);//todo
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
