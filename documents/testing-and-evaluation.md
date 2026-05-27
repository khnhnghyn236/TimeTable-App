# Testing & System Evaluation

This document outlines the testing methodology, input validation constraints, and execution logs used to evaluate the correctness, robustness, and visual reliability of the TimeTable application.

## I. Testing Methodology

Evaluation of the TimeTable application was performed through a combination of automated integration scripts (validating backend state machine correctness) and rigorous manual visual checks (validating event-driven user interface layouts and input constraints).

---

## II. Backend Integration Test Log

The project contains a dedicated test driver, `Main.java`, which acts as an integration validator for the primary scheduling workflows (resource creation, booking limits, and waitlist auto-promotion logic). 

### 1. Test Scenario Execution Steps
1.  **System Initialization**: Instantiates an `Administrator` ("Admin Alice") and three `Student` users ("Truong Ba Ky", "Nguyen Hong Khanh", "Tran Trong Tai").
2.  **Resource Provisioning**: Admin Alice dynamically spawns a physical study room (`Library Study Room A`) with a seating capacity of 2.
3.  **Timeslot Allocation**: A scheduling block is created for the room spanning "10:00 AM - 11:00 AM".
4.  **Booking Contention**:
    *   *Student 1 (Ky)* books the slot. Confirmed size (1) < capacity (2) ➔ **Success**.
    *   *Student 2 (Khanh)* books the slot. Confirmed size (2) < capacity (2) ➔ **Success**.
    *   *Student 3 (Tai)* books the slot. Confirmed size (2) == capacity (2) ➔ **Full**. Student 3 is pushed to the FIFO waitlist queue.
5.  **Cancellation & Handshake Validation**: Student 1 (Ky) cancels his confirmed booking. The system immediately removes him, queries the waitlist queue, pops Student 3 (Tai), and automatically promotes him to the confirmed list.

### 2. Empirical Execution Log Output
Running the Maven executor `mvn compile exec:java "-Dexec.mainClass=main.Main"` outputs the following trace:

```text
--- SYSTEM INITIALIZATION ---
Admin Admin Alice created resource: Library Study Room A (Capacity: 2)
Professor created slot: 10:00 AM - 11:00 AM

--- STUDENTS START BOOKING ---
SUCCESS: Truong Ba Ky booked 10:00 AM - 11:00 AM
SUCCESS: Nguyen Hong Khanh booked 10:00 AM - 11:00 AM
FULL: Tran Trong Tai placed on waitlist.

--- NO-SHOW / CANCELLATION EVENT ---
CANCELLED: Truong Ba Ky lost their slot.
AUTO-ASSIGN: Slot given to waitlisted student -> Tran Trong Tai
```

**Evaluation**: The log matches the expected mathematical behavior of the First-In-First-Out (FIFO) queue and resources bounds, verifying correctness of the booking state machines.

---

## III. GUI Security & Input Validation Tests

Manual visual tests were executed to ensure correct boundary checks and defensive programming behaviors inside the JavaFX views:

### 1. Sign-Up Form Formats & Injections
*   **Validation Check**: The sign-up submission validates text inputs before model instantiation. 
    *   *Constraint A (Empty Fields)*: Attempting to submit empty forms triggers a visual shake animation on the form and displays a red status label: `"Please fill in all fields."`
    *   *Constraint B (Email Syntax)*: Submitting invalid email configurations (e.g. missing `@` or domain) displays: `"Please enter a valid email address."`
    *   *Constraint C (Delimiter Injection)*: The serialization database uses pipe delimiters (`|`). Inputting `|` in any text box blocks submission and displays: `"Please do not use the | character."`
    *   *Constraint D (Duplicate Detection)*: Inputting an existing ID or email is checked against `allUsers` and triggers: `"Account ID already exists."` or `"Email address already exists."`

### 2. Authentication Integrity
*   **Remember-Me Check**: Selecting "Remember my ID" writes the validated ID to local disk (`remembered_user.txt`). Returning to the login screen pre-fills the login box.
*   **Encrypted Passwords**: Form passwords are run through the SHA-256 secure hash function before matching against the CSV values, verifying that cleartext passwords are not stored in memory.

### 3. Grid Collisions & Proposal Constraints
*   **Double-Booking Overlaps**: Proposing a timeslot on the `StaffDashboard` calendar grid checks room availability and faculty member logs:
    *   If a physical room is already booked by another user during those hours, the grid paints the block red (`#EF4444`) with text `❌ Booked by Other` and blocks interaction.
    *   If the faculty member is scheduled in another room, the cell paints red with text `❌ Booked by You` to prevent double-booking.
*   **Admin Approval Locks**: Newly painted slots remain amber (`#F59E0B` Pending) and are invisible to students until approved by an administrator, validating correct pipeline sequence flow.
