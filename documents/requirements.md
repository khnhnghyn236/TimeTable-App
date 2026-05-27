# System Requirements & Specifications

This document outlines the functional and non-functional requirements of the TimeTable scheduling system, extracted strictly from the implemented code.

## I. Functional Requirements (FR)

### FR1: Role-Based Access Control & User Lifecycle
*   **Sign-Up Protocol**: Unregistered guests can request either a `Student` or an `AcademicStaff` account (`SignUpScreen`).
*   **Approval Gatekeeping**: Newly created accounts are marked `isApproved = false` and suspended in the pending request list (`pendingUsers`). Administrators must review and either Approve or Decline the request (`AdminDashboard`).
*   **Persistent Remember-Me**: The system supports saving the validated user credentials in local text files (`UserPersistence`) to automatically populate forms during return sessions.

### FR2: Administrative Space Management (Resource Catalog)
*   **Resource Creation**: Administrators can create physical campus resources (`Resource`) specifying a unique Name and seating Capacity.
*   **Catalog Editing**: Admins can modify resource configurations (updating Name/Capacity dynamically in the CSV database) or delete them permanently.

### FR3: Faculty Schedule Grid Definition
*   **Interactive Time Blocking**: Academic Staff can propose availability slots on an interactive 7-day calendar grid (`StaffDashboard`). Staff define working hours in 30-minute intervals using a click-and-drag painter.
*   **Clash Prevention**: The scheduling grid dynamically enforces overlapping constraints. If a physical room is already booked by another user, or if the faculty member is double-booked, the system blocks insertion and changes cell style to visual error red (`#EF4444`).
*   **Approval Pipeline**: Newly painted timeslots are created with status `PENDING` and must be validated by an Admin before becoming visible to students.

### FR4: Student Appointment Booking & Waitlist Queue
*   **Dynamic Browsing**: Students can view an active, chronologically sorted catalog of `APPROVED` timeslots.
*   **Waitlisting Mechanics**:
    *   *Path A (Available Seating)*: If slot bookings are below the capacity limit, the student is added to `confirmedStudents`.
    *   *Path B (Full Capacity)*: If the slot is full, the student is pushed to the First-In-First-Out (FIFO) waitlist queue.
*   **Auto-Promotion**: If a confirmed student cancels, the system automatically pops (`poll()`) the next student from the FIFO waitlist, promotes them to the confirmed list, generates a notification, and writes the state to disk.

### FR5: Check-In & No-Show Grace Period Execution
*   **Student Arrival Check-In**: Students can record their arrival at the scheduled session (`StudentDashboard`), moving them to `checkedInStudents`.
*   **Grace Expiration**: The scheduling core supports a 15-minute grace period function (`processNoShows`). Students who have not checked in within 15 minutes of the start time have their bookings cancelled, triggering waitlist promotion.

### FR6: Real-Time Immutable Alerts
*   **Event-Driven Logs**: The system generates notifications (`Notification`) capturing timestamps, targets, and messages during key state changes (user approved, timeslot approved/declined, waitlist promotion).

---

## II. Non-Functional Requirements (NFR)

### NFR1: Data Persistence
*   **Local File I/O**: The system persists all state elements locally without database management overhead. Data is written to `/data/` as pipe-delimited (`|`) CSV and text files to prevent conflicts if user inputs contain commas.

### NFR2: Algorithmic Performance
*   **Verification Speed**: User authentication checks must execute in $O(1)$ average time complexity using a custom hashing map (`CustomHashMap`).
*   **Chronological Sorting**: Timeslot arrays must be sorted in $O(n \log n)$ average time using a Binary Search Tree (`CustomBST`) with In-Order Traversal.

### NFR3: Usability & Aesthetic Design
*   **Unified Visual Style**: High visual harmony driven by a defined color palette (`AppStyles`) and custom-designed desktop views. Responsive layouts handle dashboard transitions cleanly.
*   **User Feedback**: Visual field animations (shake transitions) alert users of failed password or empty field validations.

### NFR4: Data Security
*   **Cryptographic Hashing**: Cleartext passwords are never saved. The system runs an implementation of the secure SHA-256 algorithm via Java's `MessageDigest` utility to compute and store secure hash digests.
