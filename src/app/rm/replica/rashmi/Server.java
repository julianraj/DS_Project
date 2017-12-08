package app.rm.replica.rashmi;

import app.Helper;
import app.Util;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements CampusOperations {

    public HashMap<String, HashMap<Integer, List<RoomRecordClass>>> hashmap;
    public HashMap<String, Integer> student_booking;
    public HashMap<Integer, String[]> queue;
    AtomicInteger expected;
    String campus_name;
    DatagramSocket aSocket = null;
    int booking_id_seq = 1;
    boolean notKilled = true;
    boolean hasError = false;
    int replicaIndex;

    public Server(HashMap<String, HashMap<Integer, List<RoomRecordClass>>> hashmap, HashMap<String, Integer> student_booking, String campus, HashMap<Integer, String[]> queue, AtomicInteger expected, int replicaIndex, boolean hasError) {
        this.hashmap = hashmap;
        this.campus_name = campus;
        this.student_booking = student_booking;
        this.queue = queue;
        this.expected = expected;
        this.hasError = hasError;
        this.replicaIndex = replicaIndex;

        /*List<RoomRecordClass> list_time_slots = new ArrayList<>();
        list_time_slots.add(new RoomRecordClass("9-12", null, null));
        list_time_slots.add(new RoomRecordClass("12-15", null, null));
        list_time_slots.add(new RoomRecordClass("15-18", null, null));
        HashMap<Integer, List<RoomRecordClass>> room_record = new HashMap<>();
        room_record.put(201, list_time_slots);
        this.hashmap.put("11-11-2017", room_record);*/
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                UDPConnection();
            }
        }.start();
    }

    public void stop() {
        notKilled = false;
        if (aSocket != null) {
            aSocket.close();
            aSocket = null;
        }
    }


    @Override
    public String createRoom(String admin_id, int room_number, String date, String[] slots) {
        synchronized (hashmap) {
            HashMap<Integer, List<RoomRecordClass>> rr = hashmap.get(date);
            if (rr == null) rr = new HashMap<>();

            List<RoomRecordClass> list_of_rr = rr.get(room_number);
            if (list_of_rr == null) {
                list_of_rr = new ArrayList<>();
            }

            for (int i = 0; i < slots.length; i++) {
                RoomRecordClass room_record = new RoomRecordClass(slots[i], null, null);
                boolean has_record = false;
                for (int j = 0; j < list_of_rr.size(); j++) {
                    has_record = list_of_rr.get(j).time_slot.equals(slots[i]);
                    if (has_record) break;
                }

                if (!has_record) {
                    list_of_rr.add(room_record);
                }
            }

            rr.put(room_number, list_of_rr);
            hashmap.put(date, rr);
        }
        String parameters = "Room Number: " + room_number + " Date: " + date + " Slots: " + Arrays.deepToString(slots);
        Helper.writelog("Server", null, campus_name, "Create Room", parameters, true, "Room Created");//todo
        return ("successss");
    }

    @Override
    public String deleteRoom(String admin_id, int room_number, String date, String[] slots) {
        synchronized (hashmap) {
            HashMap<Integer, List<RoomRecordClass>> rr = hashmap.get(date);
            if (rr == null || rr.isEmpty()) {
                String message = "There are no rooms in the given date to delete";
                String parameters = "Room Number: " + room_number + " Date: " + date + " Slots: " + Arrays.deepToString(slots);
                Helper.writelog("Server", null, campus_name, "Delete Room", parameters, true, message);//todo
                System.out.println(message);
                return "failed";
            }

            List<RoomRecordClass> list_of_rr = rr.get(room_number);
            System.out.println();
            if (list_of_rr == null || list_of_rr.isEmpty()) {
                String message = "There are no rooms in the given date and room number to delete";
                String parameters = "Room Number: " + room_number + " Date: " + date + " Slots: " + Arrays.deepToString(slots);
                Helper.writelog("Server", null, campus_name, "Delete Room", parameters, true, message);//todo
                System.out.println(message);
                return "failed";
            }
            boolean has_record = false;
            for (int i = 0; i < slots.length; i++) {

                for (int j = 0; j < list_of_rr.size(); j++) {
                    has_record = list_of_rr.get(j).time_slot.equals(slots[i]);
                    if (has_record) {
                        if (list_of_rr.get(j).booked_by != null) {
                            int booking_number = student_booking.get(list_of_rr.get(j).booked_by);

                            booking_number -= 1;
                            student_booking.put(list_of_rr.get(j).booked_by, booking_number);
                        }
                        list_of_rr.remove(list_of_rr.get(j));
                        break;
                    }
                }

            }
            if (has_record) {
                String parameters = "Room Number: " + room_number + " Date: " + date + " Slots: " + Arrays.deepToString(slots);
                String message = "Time slot deleted";
                Helper.writelog("Server", null, campus_name, "Delete Room", parameters, true, message);//todo
                return "success";
            } else {
                String parameters = "Room Number: " + room_number + " Date: " + date + " Slots: " + Arrays.deepToString(slots);
                String message = "Time slot not found";
                Helper.writelog("Server", null, campus_name, "Delete Room", parameters, true, message);//todo
                return "failed";
            }
        }
    }

    @Override
    public String bookRoom(String student_id, String campus_name, int room_number, String date, String slots) {
        String message;
        if (this.campus_name.equals(campus_name)) {
            synchronized (hashmap) {
                HashMap<Integer, List<RoomRecordClass>> rr = hashmap.get(date);
                if (rr == null) {
                    message = "There are no rooms in the given date to book";
                    String parameters = "Student Id: " + student_id + "Campus Name: " + campus_name + " Room Number: " + room_number + " Date: " + date + " Slots: " + slots;
                    Helper.writelog("Server", null, campus_name, "Book Room", parameters, false, message);//todo

                    return "failed";
                }

                List<RoomRecordClass> list_of_rr = rr.get(room_number);
                if (list_of_rr == null) {
                    message = "There are no rooms in the given date and room number to book";
                    String parameters = "Student Id: " + student_id + "Campus Name: " + campus_name + " Room Number: " + room_number + " Date: " + date + " Slots: " + slots;
                    Helper.writelog("Server", null, campus_name, "Book Room", parameters, false, message);//todo

                    return "failed";
                }


                String booking_id = "";

                boolean has_record = false;
                int booking_number = 0;
                if (student_booking.get(student_id) != null) {
                    booking_number = student_booking.get(student_id);
                }
                if (booking_number >= 3) {
                    message = "You have exceeded your quota";
                    String parameters = "Student Id: " + student_id + "Campus Name: " + campus_name + " Room Number: " + room_number + " Date: " + date + " Slots: " + slots;
                    Helper.writelog("Server", null, campus_name, "Book Room", parameters, false, message);//todo

                    return "failed";
                }
                for (int j = 0; j < list_of_rr.size(); j++) {
                    has_record = list_of_rr.get(j).time_slot.equals(slots);
                    if (has_record) {
                        if (list_of_rr.get(j).booking_id == null) {
                            booking_number += 1;
                            student_booking.put(student_id, booking_number);

                            list_of_rr.get(j).booked_by = student_id;
                            booking_id = campus_name + "B" + String.format("%04d", booking_id_seq);
                            booking_id_seq++;
                            list_of_rr.get(j).booking_id = booking_id;
                            break;
                        } else {
                            message = "This room has already been booked";
                            String parameters = "Student Id: " + student_id + "Campus Name: " + campus_name + " Room Number: " + room_number + " Date: " + date + " Slots: " + slots;
                            Helper.writelog("Server", null, campus_name, "Book Room", parameters, false, message);//todo
                            return "failed";
                        }
                    }

                }
                if (!has_record) {
                    message = "Time slot Not found";
                    String parameters = "Student Id: " + student_id + "Campus Name: " + campus_name + " Room Number: " + room_number + " Date: " + date + " Slots: " + slots;
                    Helper.writelog("Server", null, campus_name, "Book Room", parameters, false, message);//todo
                    return "failed";
                }
                message = "Your booking id is " + booking_id;
                String parameters = "Student Id: " + student_id + "Campus Name: " + campus_name + " Room Number: " + room_number + " Date: " + date + " Slots: " + slots;
                Helper.writelog("Server", null, campus_name, "Book Room", parameters, true, message);//todo

                return "success:" + booking_id;
            }
        } else {
            return connectUDPServerForBooking(student_id, campus_name, room_number, date, slots);
        }
    }

    private String connectUDPServerForBooking(String student_id, String campus_name, int room_number, String date, String slots) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();

            byte[] message = ("book-=" + student_id + "-=" + campus_name + "-=" + room_number + "-=" + date + "-=" + slots).getBytes();
            InetAddress aHost = InetAddress.getByName(Util.REPLICA_HOSTS[replicaIndex]);
            int serverPort = Util.getCampusPort(campus_name, replicaIndex);

            DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
            socket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);
            return new String(reply.getData()).replace("\0", "");

        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (socket != null) socket.close();
        }
    }

    @Override
    public String getTimeSlotsCampus(String date) {
        if (hasError) {
            return "success: KKL9 DVL8 WST6";
        }
        DatagramSocket socket = null;
        String reply = "";
        try {
            socket = new DatagramSocket();

            byte[] message;
            InetAddress aHost = InetAddress.getByName(Util.REPLICA_HOSTS[replicaIndex]);
            int serverPort = Util.getCampusPort("KKL", replicaIndex);
            message = ("timeslot-=" + date + "-=KKL").getBytes();
            reply += requestCampusServer(serverPort, message, aHost, socket);
            reply += " ";

            serverPort = Util.getCampusPort("DVL", replicaIndex);
            message = ("timeslot-=" + date + "-=DVL").getBytes();
            reply += requestCampusServer(serverPort, message, aHost, socket);
            reply += " ";

            serverPort = Util.getCampusPort("WST", replicaIndex);
            message = ("timeslot-=" + date + "-=WST").getBytes();
            reply += requestCampusServer(serverPort, message, aHost, socket);

            return "success: " + reply;

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            if (socket != null) socket.close();
        }
    }

    private String requestCampusServer(int serverPort, byte[] message, InetAddress aHost, DatagramSocket socket) {
        DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
        try {
            socket.send(request);
        } catch (IOException e) {
            return e.getMessage();
        }

        byte[] buffer = new byte[1000];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(reply);
        } catch (IOException e) {
            return e.getMessage();
        }
        return new String(reply.getData()).replace("\0", "");
    }

    public String connectUDPServerForTimeSlot(String date, String campus_name) {
        synchronized (hashmap) {
            HashMap<Integer, List<RoomRecordClass>> rr = hashmap.get(date);
            if (rr == null) {
                return campus_name + "0";
            }

            int available_slots = 0;
            for (Integer room_number : rr.keySet()) {

                for (RoomRecordClass roomRecordClass : rr.get(room_number)) {
                    if (roomRecordClass.booking_id == null) {
                        available_slots++;
                    }
                }
            }

            String parameters = "Campus Name: " + campus_name + " Date: " + date;
            Helper.writelog("Server", null, campus_name, "Get Time slots", parameters, true, campus_name + available_slots);//todo

            return campus_name + available_slots;
        }
    }

    @Override
    public String cancelBooking(String booking_id, String student_id) {
        if (this.campus_name.equals(booking_id.substring(0, 3))) {
            synchronized (hashmap) {
                for (String date : hashmap.keySet()) {
                    boolean has_record = false;
                    String message;
                    if (date == null) {
                        String parameters = "Booking Id: " + booking_id + " Student_id: " + student_id;
                        message = "There are no rooms in the given date to delete";
                        Helper.writelog("Server", null, campus_name, "Cancel Booking", parameters, true, message);//todo

                        return "failed";
                    }
                    for (Integer room_number : hashmap.get(date).keySet()) {
                        if (room_number == null) {
                            String parameters = "Booking Id: " + booking_id + " Student_id: " + student_id;
                            message = "There are no rooms to delete";
                            Helper.writelog("Server", null, campus_name, "Cancel Booking", parameters, true, message);//todo

                            return "failed";
                        }
                        List<RoomRecordClass> list_of_rr = hashmap.get(date).get(room_number);
                        if (list_of_rr == null) {
                            String parameters = "Booking Id: " + booking_id + " Student_id: " + student_id;
                            message = "There are no rooms in the given date and room number to delete";

                            Helper.writelog("Server", null, campus_name, "Cancel Booking", parameters, true, message);//todo
                            return "failed";
                        }


                        for (int j = 0; j < list_of_rr.size(); j++) {
                            has_record = booking_id.equals(list_of_rr.get(j).booking_id);
                            if (has_record && student_id.equals(list_of_rr.get(j).booked_by)) {
                                list_of_rr.get(j).booking_id = null;
                                list_of_rr.get(j).booked_by = null;
                                int booking_number = student_booking.get(student_id);
                                booking_number -= 1;
                                student_booking.put(student_id, booking_number);
                                String parameters = "Booking Id: " + booking_id + " Student_id: " + student_id;
                                message = "Booking Cancelled";

                                Helper.writelog("Server", null, campus_name, "Cancel Booking", parameters, true, message);//todo

                                return "success";
                            }

                        }

                    }
                }

            }
            String message = "You donot have permission to cancel booking";
            return "failed";
        } else {
            return connectUDPServerForCancelBooking(booking_id, student_id);
        }
    }

    private String connectUDPServerForCancelBooking(String booking_id, String student_id) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();

            byte[] message = ("cancel-=" + student_id + "-=" + booking_id).getBytes();
            InetAddress aHost = InetAddress.getByName(Util.REPLICA_HOSTS[replicaIndex]);
            int serverPort = Util.getCampusPort(booking_id.substring(0, 3), replicaIndex);

            DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
            socket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);
            return new String(reply.getData()).replace("\0", "");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) socket.close();
        }
        return "";
    }


    @Override
    public String changeReservation(String booking_id, String new_campus_name, int new_room_no, String new_date, String new_time_slot, String student_id) {
        String result = checkAvailability(new_campus_name, new_room_no, new_date, new_time_slot);

        boolean available = Boolean.parseBoolean(result);
        if (available) {
            //todo
            String confirm_cancel = cancelBooking(booking_id, student_id);
            if (confirm_cancel.equals("success")) {
                return bookRoom(student_id, new_campus_name, new_room_no, new_date, new_time_slot);
            } else {
                String message = "Your cancellation was not successful";
                return "failed";
            }
        } else {
            String message = "The slot is not available";
            return "failed";
        }
    }

    public String checkAvailability(String new_campus_name, int new_room_no, String new_date, String new_time_slot) {
        String availability = "false";

        if (this.campus_name.equals(new_campus_name)) {
            synchronized (hashmap) {
                HashMap<Integer, List<RoomRecordClass>> rr = hashmap.get(new_date);
                if (rr == null) {
                    String parameters = "Campus Name: " + new_campus_name + " Room Number: " + new_room_no + " Date: " + new_date + " Slots: " + new_time_slot;
                    Helper.writelog("Server", null, new_campus_name, "Check Availability", parameters, false, "There are no rooms in the given date to delete");//todo

                    return availability;
                }

                List<RoomRecordClass> list_of_rr = rr.get(new_room_no);
                if (list_of_rr == null) {
                    String parameters = "Campus Name: " + new_campus_name + " Room Number: " + new_room_no + " Date: " + new_date + " Slots: " + new_time_slot;
                    Helper.writelog("Server", null, new_campus_name, "Check Availability", parameters, false, "There are no rooms in the given date and room number to delete");//todo

                    return availability;
                }


                boolean has_record = false;
                for (int j = 0; j < list_of_rr.size(); j++) {
                    has_record = list_of_rr.get(j).time_slot.equals(new_time_slot);
                    if (has_record && (list_of_rr.get(j).booking_id == null)) {
                        availability = "true";
                    }
                }
                String parameters = "Campus Name: " + new_campus_name + " Room Number: " + new_room_no + " Date: " + new_date + " Slots: " + new_time_slot;
                Helper.writelog("Server", null, new_campus_name, "Check Availability", parameters, true, "Room Available");

                return availability;
            }
        } else {
            return connectUDPServerForAvailability(new_campus_name, new_room_no, new_date, new_time_slot);
        }
    }

    private String connectUDPServerForAvailability(String new_campus_name, int new_room_no, String new_date, String new_time_slot) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();

            byte[] message = ("checkAvailability-=" + new_campus_name + "-=" + new_room_no + "-=" + new_date + "-=" + new_time_slot).getBytes();
            InetAddress aHost = InetAddress.getByName(Util.REPLICA_HOSTS[replicaIndex]);
            int serverPort = Util.getCampusPort(new_campus_name, replicaIndex);


            DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
            socket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);
            return new String(reply.getData()).replace("\0", "");

        } catch (Exception e) {
            return (e.getMessage());
        } finally {
            if (socket != null) socket.close();
        }
    }

    private void UDPConnection() {
        try {
            aSocket = new DatagramSocket(null);
            aSocket.bind(new InetSocketAddress(InetAddress.getByName(Util.REPLICA_HOSTS[replicaIndex]), Util.getCampusPort(campus_name, replicaIndex)));
            while (notKilled) {
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    String[] data = new String(request.getData()).replace("\0", "").split("-=");

                    new MyThread(request, data).start();
                } catch (SocketException e) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (aSocket != null) aSocket.close();
        }

    }

    private class MyThread extends Thread {

        final DatagramPacket aPacket;
        final String[] data;

        public MyThread(DatagramPacket packet, String[] data) {
            aPacket = packet;
            this.data = data;
        }

        @Override
        public void run() {
            int seq;
            try {
                seq = Integer.valueOf(data[0]);

                //send ack
                String ack_message = "ack-=" + replicaIndex + "-=" + seq;
                DatagramPacket ack_packet = new DatagramPacket(ack_message.getBytes(), ack_message.length(), InetAddress.getByName(Util.SEQUENCER_HOST), Util.SEQUENCER_PORT);
                new DatagramSocket().send(ack_packet);

                if (!queue.containsKey(seq)) {
                    queue.put(seq, data);

                    if (seq == expected.get())
                        processQueue(data);
                }
            } catch (NumberFormatException e) {
                Server.this.processRequest(data, aPacket.getAddress().getHostName(), aPacket.getPort(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public void processQueue(String[] request) {
        processRequest(Arrays.copyOfRange(request, 2, request.length), Util.FRONT_END_HOST, Integer.valueOf(request[1]), false);
        queue.remove(expected.get());
        expected.incrementAndGet();
        if (queue.containsKey(expected.get()))
            processQueue(queue.get(expected.get()));
    }

    public void processRequest(String[] data, String host, int port, boolean local) {
        System.out.println(Arrays.deepToString(data));
        try {
            String message = "";
            final String function = data[0];
            if (function.equals("ping")) {
                message = "pinged";
            } else if (function.equals("create")) {
                String admin_id = data[1];
                int room_number = Integer.valueOf(data[2]);
                String date = data[3];
                String slots[] = data[4].split(",");

                message = createRoom(admin_id, room_number, date, slots);
            } else if (function.equals("delete")) {
                String admin_id = data[1];
                int room_number = Integer.valueOf(data[2]);
                String date = data[3];
                String slots[] = data[4].split(",");

                message = deleteRoom(admin_id, room_number, date, slots);
            } else if (function.equals("book")) {
                String student_id = data[1];
                String campus_name = data[2];
                int room_number = Integer.valueOf(data[3]);
                String date = data[4];
                String slots = data[5];

                message = bookRoom(student_id, campus_name, room_number, date, slots);
            } else if (function.equals("cancel")) {
                String student_id = data[1];
                String booking_id = data[2];
                message = cancelBooking(booking_id, student_id);
            } else if (function.equals("checkAvailability")) {
                String campus_name = data[1];
                int room_number = Integer.valueOf(data[2]);
                String date = data[3];
                String slots = data[4];
                message = checkAvailability(campus_name, room_number, date, slots);
            } else if (function.equals("change")) {
                String student_id = data[1];
                String old_booking_id = data[2];
                String campus_name = data[3];
                int room_number = Integer.valueOf(data[4]);
                String date = data[5];
                String slots = data[6];
                message = changeReservation(old_booking_id, campus_name, room_number, date, slots, student_id);
            } else if (function.equals("availability")) {
                String student_id = data[1];
                String date = data[2];
                message = getTimeSlotsCampus(date);
            } else {
                String campus = data[2];
                String date = data[1];
                message = connectUDPServerForTimeSlot(date, campus);
            }

            if (!local) {
                message = replicaIndex + "-=" + message;
            }
            System.out.println(message);
            DatagramPacket reply = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(host), port);

            aSocket.send(reply);
        } catch (IOException e) {

        }
    }
}
