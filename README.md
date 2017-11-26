# DS_Project
1. Add **json.jar** file from **_libs_** folder to **build-path** to handle json.

2. Booking ID format to be {campusName}B{4-digitID}. **Example: KKLB0012**

3. seperate logic into **start** and **stop** methods in server

4. udp messages format to be {4-digit-sequenceNumber}-={function}-={param1}-={param2} etc.  

   **Example:**  

   | Function | Message Format |
   | -------- | -------------- |
   | createRoom | 0001-=create-=_{adminID}_-=_{roomNum}_-=_{date}_-=_{timeslots(comma seperated)}_ |
   | deleteRoom | 0002-=delete-=_{adminID}_-=_{roomNum}_-=_{date}_-=_{timeslots(comma seperated)}_ |
   | bookRoom | 0003-=book-=_{studentID}_-=_{campusName}_-=_{roomNumber}_-=_{date}_-=_{timeslot}_ |
   | changeReservation | 0004-=change-=_{studentID}_-=_{oldBookingID}_-=_{campusName}_-=_{roomNumber}_-=_{date}_-=_{timeslot}_ |
   | cancelBooking | 0005-=cancel-=_{studentID}_-=_{bookingID}_ |
   | getAvailableTimeSlots | 0006-=availability-=_{studentID}_-=_{date}_ |

5. FrontEnd UDP Message to Replica Manager to be of formats  
   - **error:not-available** if server is not available.
   - **error:{expected_correct_answer}** if wrong response.
  
6. Server response messages:
   
   | Function | Success Message | Failure Message |
   | -------- | --------------- | --------------- |
   | createRoom | success | failed |
   | deleteRoom | success | failed |
   | bookRoom | success: _{bookingID}_ | failed |
   | changeReservation | success: _{bookingID}_ | failed |
   | cancelBooking | success | failed |
   | getAvailableTimeSlots | success: _KKL0 DVL0 WST0_ | failed |

7. Ports

   | Component | Port |
   | --------- | ---- |
   | FrontEnd | 4001 |
   | Sequencer | 5001 |
   | ReplicaManger | 6001 |
   | KKL Server | 7001 |
   | DVL Server | 7002 |
   | WST Server | 7003 |
   
8. JSON format for representing database is available in ![RR-format.json](./RR-format.json) file

> P.S. _{ }_ in above formats needs to be excluded in actual implementation.