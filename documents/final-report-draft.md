# COMP1020 Term Project Final Report: Academic Draft

---

## Cover Page Details
*   **Term Project Title**: TimeTable: A Centralized Campus Appointment Scheduling Platform
*   **Course**: COMP1020 Object-Oriented Programming & Data Structures — Spring 2026
*   **College**: College of Engineering and Computer Science, VinUniversity
*   **Team Name**: Team Macau (Needs confirmation from team)
*   **Team Members**:
    1.  *Truong Ba Ky* — ID: [Needs confirmation from team]
    2.  *Nguyen Hong Khanh* — ID: [Needs confirmation from team]
    3.  *Nguyen Trong Nguyen* — ID: [Needs confirmation from team]
    4.  *Tran Trong Tai* — ID: [Needs confirmation from team]
*   **Submission Date**: June 2, 2026

---

## Section 1: Introduction and Project Overview

### 1.1 Problem Statement and Motivation
In university ecosystems, coordinating academic sessions, student advising, and physical resource booking (such as library study rooms or consultation hubs) presents significant operational challenges. Manual scheduling is prone to time conflicts, suffers from a lack of prioritized waiting lists when capacities are exceeded, and incurs overhead due to disconnected email threads. To address these inefficiencies, this project introduces **TimeTable**, an event-driven desktop platform that automates appointment scheduling, resolves spatial-temporal conflicts, and manages priority waiting queues without database overhead.

### 1.2 System Scope & Actor Roles
The application is structured around three core university roles, whose actions are synchronized through a centralized graphical framework:
*   **Students**: Browse active, approved timeslots sorted chronologically; book appointments; join a First-In-First-Out (FIFO) queue when rooms are full; record attendance via check-in; and cancel reservations.
*   **Academic Staff (Faculty)**: Manage an interactive weekly calendar grid, paint availability schedules using a click-and-drag event listener, edit or delete proposed timeslots, and track student booking registries.
*   **Administrators**: Configure the physical campus resource catalog (defining rooms and capacities), approve proposed faculty timeslots, and manage the pending registration queue of new users.

### 1.3 Key Implemented Features
1.  **Chronological BST Display**: Timeslots are sorted and rendered in continuous chronological blocks.
2.  **FIFO Waitlist Queue**: Fully automated waitlist management with automatic student promotion upon cancellation.
3.  **Visual Calendar Grid**: A click-and-drag grid scheduler that visually alerts staff to room and schedule conflicts.
4.  **Security Gatekeeping**: Administrative review queues for new users, combined with SHA-256 secure password hashing.
5.  **Robust Persistence**: A pipe-delimited local storage engine that prevents database corruption when inputs contain commas.

*Refer to the macro application flowchart in **[system-workflow.md](file:///c:/Users/ADMIN/Desktop/TimeTable-App/documents/system-workflow.md)** for a visual trace of the overall lifecycle from launch to execution.*

---

## Section 2: System Requirements and Specifications

### 2.1 Functional Requirements (FR)
*   **FR1: User Authentication & Gatekeeping**: The system supports user signup (`SignUpScreen`) and secure login (`LoginScreen`). Accounts are created in a pending state (`isApproved = false`) and must be approved by an Admin (`AdminDashboard`) before accessing the dashboards. Session credentials can be saved locally (`UserPersistence`).
*   **FR2: Administrative Space Management**: Administrators can add, edit, or delete bookable rooms (`Resource`) in the system catalog, specifying unique names and seat capacities.
*   **FR3: Faculty Availability Grid Proposal**: Faculty propose timeslots on a 7-day Weekly Grid in 30-minute intervals (`StaffDashboard`). Proposed slots remain `PENDING` until validated by an Admin.
*   **FR4: Scheduling Clash Detection**: The Weekly Grid checks room availability and faculty schedules, coloring clashing blocks red (`#EF4444`) and blocking overlapping timeslots.
*   **FR5: Priority Booking & Waitlist Handshake**: Students book `APPROVED` timeslots. If seat limits are reached, students are placed in a FIFO waitlist queue.
*   **FR6: Cancellation & Auto-Promotion**: Cancelling a booking pops the head of the waitlist queue, promotes them to confirmed status, and writes the state to disk.
*   **FR7: Check-In & No-Show Grace Period**: Students record attendance upon arrival. If they do not check in within 15 minutes of the start time, `processNoShows()` cancels their booking and promotes the next waitlisted student.

### 2.2 Non-Functional Requirements (NFR)
*   **NFR1: Persistence Reliability**: Local storage uses pipe-delimited (`|`) formatting inside `/data/` flat-files to bypass comma-parsing issues.
*   **NFR2: Performance Efficiency**: User authentication verifies credentials in $O(1)$ average time. Timeslot sorting operates in $O(n \log n)$ time.
*   **NFR3: Usability & Layout Consistency**: Dashboards share programmatic layouts, and form validations are enhanced with visual warning animations (field shakes).
*   **NFR4: Data Cryptography**: Cleartext passwords are hashed using a secure SHA-256 algorithm via `java.security.MessageDigest` before saving to disk.

---

## Section 3: System Design and Architecture

### 3.1 Model-View-Controller (MVC) Separation
The application enforces a decoupled MVC architecture:
*   **Model**: Classes inside `users` (`User`, `Student`, `AcademicStaff`, `Administrator`) and `scheduling` (`Resource`, `TimeSlot`, `Notification`) capture business logic and variables, with no dependency on JavaFX graphics libraries.
*   **View**: Layout panels in `gui` (`LoginScreen`, `SignUpScreen`, dashboards) programmatically build layout controls. Shared themes and visual states are centralized in `AppStyles.java`.
*   **Controller**: Event listeners registered on view elements intercept user actions, manipulate in-memory model instances, and trigger updates to the local database. Navigation is managed by `AppSchedulerGUI.java`.

### 3.2 OOP Design Patterns & Principles
*   **Abstraction**: `User` is an abstract base class, hiding credentials and password verification logic while blocking direct instantiation.
*   **Inheritance**: Subclasses `Student`, `AcademicStaff`, and `Administrator` inherit common attributes from `User`, eliminating duplicate code.
*   **Encapsulation**: State fields are marked `private` or `protected`. State changes are restricted to domain methods (e.g., `bookSlot()`, `cancelBooking()`, `checkIn()`), protecting internal lists.
*   **Polymorphism**: The controller stores users in a generic collection of `User`, dynamically resolving subclasses at runtime using type-checks (`instanceof`) to load the correct role dashboards.
*   **Interface Realization**: `TimeSlot` implements the standard `Comparable<TimeSlot>` interface, overriding `compareTo()` to dictate chronological ordering.

*Refer to **[class-uml.md](file:///c:/Users/ADMIN/Desktop/TimeTable-App/documents/class-uml.md)** for a streamlined UML diagram showing these relationships.*

---

## Section 4: Data Structures and Algorithms

### 4.1 Custom Collections & Asymptotic Analysis
1.  **`CustomHashMap<K, V>`**: A generic Hash Map using **Separate Chaining** for collision resolution. Singly-linked list nodes (`Entry<K, V>`) handle collisions. Provides $O(1)$ average-case lookup times during user validation ($O(n)$ worst-case under extreme collisions).
2.  **`CustomBST<T extends Comparable<T>>`**: A generic Binary Search Tree used to aggregate system timeslots. Recursive binary insertion achieves $O(\log n)$ average-case complexity ($O(n)$ worst-case if unbalanced). Recursive **In-Order Traversal** retrieves sorted timeslots in $O(n)$ time.
3.  **Waitlist Queue**: A First-In-First-Out (FIFO) queue represented by a double-linked node chain (`java.util.LinkedList`) inside `TimeSlot.java`. Provides $O(1)$ insertions (`add()`) and queue polling (`poll()`).

#### Table 4.1: Asymptotic Complexity Profiles
| Structure | Average-Case Insertion | Average-Case Search | Worst-Case Search | Primary Application |
| :--- | :--- | :--- | :--- | :--- |
| **`CustomHashMap`** | $O(1)$ | $O(1)$ | $O(n)$ | Fast user session validation |
| **`CustomBST`** | $O(\log n)$ | $O(\log n)$ | $O(n)$ | Sorted timeslot catalog aggregation |
| **`FIFO Queue`** | $O(1)$ | $O(1)$ | $O(1)$ | Priority student waitlist queue |

### 4.2 Key System Algorithms
*   **SHA-256 Cryptographic Hashing**: Converts cleartext passwords to 64-character hexadecimal hashes using `java.security.MessageDigest` before saving to disk. Time complexity is linear at $O(m)$ relative to password length.
*   **Timeslot Overlap Detection**: Evaluates date and time intersections:
    $$\text{Overlap} \iff (\text{Date}_A = \text{Date}_B) \land (\text{Start}_A < \text{End}_B) \land (\text{End}_A > \text{Start}_B)$$
*   **Session Normalization**: Linear sorting and merging algorithm (`normalizeTimeSlots()`) which combines adjacent 30-minute availability blocks of the same Room, Creator, and Title into single, continuous blocks. Operates in $O(n \log n)$ time.

---

## Section 5: Implementation Details

### 5.1 Technology Configurations
*   **Development Platform**: Java JDK 21 and OpenJFX 21 SDK.
*   **Compilation Tool**: Maven-managed compile and exec plugins.
*   **Database Schema**: CSV and pipe-delimited (`|`) records inside local flat-files in the `/data/` folder, protecting parsing integrity when inputs contain commas.

### 5.2 Key Design Decisions
*   **Decoupled Style System**: Centralizing layouts, buttons, and color themes in `AppStyles.java` ensures high visual consistency.
*   **Programmatic Grids**: Using raw JavaFX layout constraints (`GridPane`, `ColumnConstraints`) to draw weekly calendar schedulers without FXML files.
*   **Drag Event Listeners**: Using interactive drag-selection handlers (`setOnDragDetected`, `setOnMouseDragEntered`) on the grid cells to paint availability blocks.

---

## Section 6: Testing and Evaluation

### 6.1 Programmatic Backend Integration Verification
The test driver `Main.java` serves as a complete integration script, verifying resource creation, slot allocation, seating capacity limits, waitlist queue placement, and auto-promotion:

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
*Refer to **[interaction-map.md](file:///c:/Users/ADMIN/Desktop/TimeTable-App/documents/interaction-map.md)** for a sequence diagram of this interaction.*

### 6.2 GUI Security & Input Validation Tests
*   **Delimiter Character Protection**: Form fields restrict the use of the `|` database delimiter, displaying: `"Please do not use the | character."`
*   **Field Formats**: Sign-up inputs validate email syntax using regex and detect empty inputs, shaking the UI field to alert the user.
*   **Overlapping Warnings**: Proposing clashing timeslots on the faculty grid displays: `❌ Booked by Other` or `❌ Booked by You` in red (`#EF4444`).

---

## Section 7: Challenges and Solutions

*   **Challenge 1: Code-Based Layouts without FXML/SceneBuilder**:
    *   *Solution*: Encapsulated custom styled buttons and labels in `AppStyles.java` and nested layout structures (`ScrollPane`, `HBox`, `VBox`) to achieve consistent proportions.
*   **Challenge 2: Overlapping Room Clashes & Contiguous Time Merges**:
    *   *Solution*: Designed the math intersection formula (`overlaps()`) and integrated the sorting/merging scheduler (`normalizeTimeSlots()`) to combine matching intervals.
*   **Challenge 3: Waitlist Synchronization & Promotions**:
    *   *Solution*: Intercepted cancellation events in the View, routing the returned reference of the promoted student through immediate `addNotification()` actions and writing the new states to disk.

---

## Section 8: Conclusion and Limitations

### 8.1 Final Outcomes & Strengths
The platform successfully implements clean MVC separation, custom high-performance collections (`CustomHashMap`, `CustomBST`), and robust automated booking and waitlist promotions. The Weekly Grid provides an intuitive scheduler for faculty, backed by robust validation systems.

### 8.2 System Limitations
*   **Flat-File Concurrency Constraints**: Local I/O is susceptible to database lock issues under high write concurrency.
*   **Client Architecture**: Desktop-bound execution limits cross-device synchronization and accessibility.

### 8.3 Future Improvements
*   **Database Integrations**: Transitioning to a SQL server (e.g., MySQL, PostgreSQL) utilizing JPA/Hibernate for robust transactional locking.
*   **Client-Server Migration**: Migrating the system to a RESTful Spring Boot backend server and a React/HTML5 web browser frontend client.

---

## Section 9: Team Contributions

The development process was evenly divided among members of **Team Macau** (Needs confirmation from team):
*   **Truong Ba Ky**: Engineered custom `CustomBST` and waitlist FIFO queue algorithms; structured `TimeSlot` booking and cancellation promotions.
*   **Nguyen Hong Khanh**: Implemented `CustomHashMap` user databases, SHA-256 credential hashing, and CSV flat-file database serialization.
*   **Nguyen Trong Nguyen**: Engineered the Administrator dashboard, room catalog creation methods, and account approval queues.
*   **Tran Trong Tai**: Built the interactive 7-day Weekly Grid calendar view, click-and-drag painting handlers, and timeslot merges.

*Individual weight distributions for peer grading: [Needs confirmation from team].*

---

## Section 10: References
1.  Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.
2.  Oracle Corporation. (2025). *JavaFX API Documentation*. Retrieved from https://openjfx.io/
3.  Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2009). *Introduction to Algorithms* (3rd ed.). MIT Press.

---

## Section 11: Appendix

### A. System Architecture Models
*   *UML Class Diagram*: Fully detailed in **[class-uml.md](file:///c:/Users/ADMIN/Desktop/TimeTable-App/documents/class-uml.md)**.
*   *Sequence Diagrams*: Dynamic component mapping detailed in **[interaction-map.md](file:///c:/Users/ADMIN/Desktop/TimeTable-App/documents/interaction-map.md)**.
*   *Workflows & State Charts*: Outlined in **[system-workflow.md](file:///c:/Users/ADMIN/Desktop/TimeTable-App/documents/system-workflow.md)**.
