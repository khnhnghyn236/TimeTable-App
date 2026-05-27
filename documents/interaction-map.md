# System Interaction & Sequence Diagram

This document describes how components inside the TimeTable application interact during the core scheduling and booking use cases, structured across architectural layers: **UI View**, **Controller**, **Model**, and **Storage/Persistence** layers.

## I. Model-View-Controller (MVC) Architecture

To enforce clean separation of concerns, the application structure separates state, representation, and coordination logic:

*   **Model (Domain State)**: Encapsulated inside `users` and `scheduling` packages. These classes (`User`, `Student`, `AcademicStaff`, `Administrator`, `Resource`, `TimeSlot`, `Notification`) define data fields and strict domain methods. They contain no GUI code and are completely independent of JavaFX.
*   **View / UI Layer**: Created programmatically in the `gui` package. Classes like `LoginScreen`, `SignUpScreen`, and the dashboards construct the layout controls (`GridPane`, `HBox`, `ListView`, `Button`, `TextField`) and define the visual theme using static configurations from `AppStyles.java`.
*   **Controller Layer**: Managed by `AppSchedulerGUI` (which initiates the application, hosts central observable references, and coordinates scene transitions) and the interactive handlers within the dashboards. They register event listeners (e.g., button actions, mouse clicks, drag handlers) on the Views, manipulate the Models based on user inputs, and save changes to disk.
*   **Storage / Persistence Layer**: Controlled by the utility `DataManager` which writes and reads pipe-delimited data directly to local flat-files inside `/data/`.

---

## II. Sequence Diagram: Booking & Waitlist Queue Promotion

This diagram displays the dynamic transaction flow when **Student Alice** cancels a confirmed booking in a fully booked room, triggering the system to automatically promote **Student Bob** from the waitlist queue to a confirmed seat:

```mermaid
sequenceDiagram
    autonumber
    actor Alice as Student Alice (Confirmed)
    actor Bob as Student Bob (Waitlisted)
    
    box rgb(240, 248, 255) UI / View Layer
        participant View as StudentDashboard
    end
    
    box rgb(255, 240, 245) Controller Layer
        participant App as AppSchedulerGUI
    end
    
    box rgb(240, 255, 240) Model Layer
        participant Slot as TimeSlot
    end
    
    box rgb(255, 255, 240) Storage / Persistence Layer
        participant DB as DataManager
    end

    Note over Alice, DB: Target 'TimeSlot' is fully booked (Capacity: 1). Alice holds the confirmed seat. Bob is on the Waitlist.

    Alice->>View: Clicks "Cancel / Leave Waitlist" on her dashboard
    activate View
    
    View->>App: confirmAction("Confirm Cancellation", ...)
    activate App
    App-->>View: Returns true (Confirmed by Alice)
    deactivate App
    
    View->>Slot: cancelBooking(Alice)
    activate Slot
    Slot->>Slot: confirmedStudents.remove(Alice)
    Note over Slot: Confirmed size < Capacity. Waitlist queue is checked.
    
    Slot->>Slot: waitlist.poll() [Bob is popped from FIFO Queue]
    Slot->>Slot: confirmedStudents.add(Bob)
    Slot-->>View: Returns Bob (Promoted Student Reference)
    deactivate Slot

    View->>App: addNotification(Bob.getUserId(), "A spot opened up! ...")
    activate App
    App->>DB: saveNotifications(systemNotifications)
    activate DB
    DB-->>App: Notifications written to data/notifications_data.txt
    deactivate DB
    deactivate App

    View->>DB: saveState(systemTimeSlots)
    activate DB
    DB-->>View: Timeslots written to data/appointments_data.txt
    deactivate DB

    View->>View: myBookedSlots.remove(selectedSlot)
    View->>App: updateStudentUIList()
    activate App
    App-->>View: Refreshes sorted list views
    deactivate App
    
    View-->>Alice: Updates UI (Booking removed from list)
    deactivate View
```
