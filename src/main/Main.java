package main;

import users.Student;
import users.AcademicStaff;
import users.Administrator;
import scheduling.Resource;
import scheduling.TimeSlot;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- SYSTEM INITIALIZATION ---");
        
        // 1. Create our Users
        Administrator admin = new Administrator("A001", "Admin Alice");
        AcademicStaff prof = new AcademicStaff("P001", "Prof. Bob");
        
        Student student1 = new Student("V001", "Truong Ba Ky");
        Student student2 = new Student("V002", "Nguyen Hong Khanh");
        Student student3 = new Student("V003", "Tran Trong Tai");

        // 2. Admin creates a small study room (Capacity: 2)
        Resource studyRoom = admin.createResource("Library Study Room A", 2);

        // 3. Staff creates a time slot for that room
        TimeSlot morningSlot = new TimeSlot("10:00 AM - 11:00 AM", studyRoom);
        System.out.println("Professor created slot: 10:00 AM - 11:00 AM");
        
        System.out.println("\n--- STUDENTS START BOOKING ---");
        
        // 4. Students book (Capacity is 2, so the 3rd should go to the waitlist)
        morningSlot.bookSlot(student1);
        morningSlot.bookSlot(student2);
        morningSlot.bookSlot(student3); // This will trigger the waitlist!

        System.out.println("\n--- NO-SHOW / CANCELLATION EVENT ---");
        
        // 5. Student 1 cancels, triggering the automatic waitlist assignment
        morningSlot.cancelBooking(student1); 
    }
}