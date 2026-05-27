# Technical Report Notes & Codebase Analysis

This document contains raw research notes, package structures, file analysis, and mappings linking the Java source code to the LaTeX report template.

## I. Project Structure & Core Assets
*   **Total Source Packages**: 5
*   **Total Java Classes**: 18
*   **Core UI Theme**: Centralized inside `gui.AppStyles` (using hex codes: `#0C447C` Navy, `#185FA5` Mid Blue, `#10B981` Green, `#EF4444` Red, `#F59E0B` Amber, `#F5F7FA` Light Grey page backgrounds).
*   **File-Based Storage**: Kept inside `/data/` as text files (`users_data.csv`, `resources_data.csv`, `appointments_data.txt`, `notifications_data.txt`).

## II. Codebase Directory Mapping
| Package | Class File | Responsibility |
| :--- | :--- | :--- |
| **datastructures** | `CustomBST.java` | Chronological BST timeslot sorter (In-Order Traversal) |
| | `CustomHashMap.java` | Separate chaining hash map for $O(1)$ user validation |
| **users** | `User.java` | Abstract base class; stores name, email, credentials, and SHA-256 hash logic |
| | `Student.java` | Concrete class representing the student booking persona |
| | `AcademicStaff.java` | Concrete class representing faculty scheduling persona |
| | `Administrator.java` | Concrete class representing admin control persona; defines resource factory |
| **scheduling** | `Resource.java` | Represents physical rooms/offices and their seat capacity limits |
| | `TimeSlot.java` | Core scheduling block; binds date/times, handles waitlists, check-ins, cancels |
| | `Notification.java` | Immutable alert logs for system actors |
| | `Appointment.java` | Association mapping representation between a student and a slot |
| **main** | `AppSchedulerGUI.java` | Primary controller coordinating FX scenes, global lists, and routing logic |
| | `DataManager.java` | Flat file persistence engine; handles save/load state loops |
| | `UserPersistence.java` | Remembers the session credentials |
| | `Main.java` | Integration test driver for console output generation |
| **gui** | `LoginScreen.java` | Handles user authentication and password hiding/showing |
| | `SignUpScreen.java` | Handles account registrations (pending admin validation) |
| | `AdminDashboard.java` | Tabbed controller for rooms, slots approvals, and requests |
| | `StaffDashboard.java` | Dynamic weekly scheduling calendar grid, drag proposals, list editor |
| | `StudentDashboard.java` | Student appointment browse-book cards, check-in, and cancel buttons |
| | `AppStyles.java` | Shared aesthetic configurations and status visual transitions |

## III. LaTeX Section Mapping Blueprint
1.  **Section 1: Introduction**: Outline VinUniversity scheduling challenges; introduce `AppSchedulerGUI`'s multi-persona workflow.
2.  **Section 2: Specifications**: Outline requirements (functional checks like automated queue promotion, non-functional checks like secure SHA-256 password hashing).
3.  **Section 3: Design**: Detail the Model-View-Controller (MVC) pattern and highlight 5 primary OOP principles.
4.  **Section 4: Data Structures**: Deep-dive into `CustomBST` ($O(\log n)$), `CustomHashMap` ($O(1)$), and waitlist FIFO `LinkedList` queue. Detail overlaps/merges.
5.  **Section 5: Implementation**: Code configurations, compiling environments, pipe-separated (`|`) parsing algorithms.
6.  **Section 6: Testing**: Visual edge checks, input validations, `Main.java` integration console logs.
7.  **Section 7: Challenges**: Multi-session coordinate mapping on weekly grids, no-show promotions.
8.  **Section 8: Conclusion**: Strengths, local flat-file storage bottlenecks, relational database expansions.
9.  **Section 9: Contributions**: Team Macau task division (Ky, Khanh, Nguyen, Tai).
10. **Section 10: References**: Citations for collections, design schemas, OpenJFX controls.
11. **Section 11: Appendix**: Visual Class UML, Sequence flows, terminal testing logs.

## IV. Needs Confirmation Items
*   *Student IDs*: Concrete IDs for Ky, Khanh, Nguyen, and Tai are currently placeholders (`[Student ID]`) in the LaTeX file and need direct user inputs before compiling.
*   *Team Name*: Presumed "Team Macau" as per `README.md`.
*   *Preload Data*: Default admin accounts (`V202502310`, `V202502059`, etc.) are pre-configured with password `admin123`.
