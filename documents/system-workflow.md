# System Workflow & State Lifecycle

This document describes the operational workflows for each user role and outlines the lifecycle states of critical domain entities inside the TimeTable application.

## I. Mermaid Flowchart: Overall Application Workflow

This flowchart outlines the complete operational lifecycle from application launch, de-serialization of persistent flat-files, login gatekeeping, role-based routing, and primary actions within each dashboard:

```mermaid
flowchart TD
    %% Define Styles
    classDef startEnd fill:#0C447C,stroke:#0C447C,stroke-width:2px,color:#fff;
    classDef process fill:#E2E8F0,stroke:#64748B,stroke-width:1px,color:#1A1A2E;
    classDef decision fill:#FEF3C7,stroke:#F59E0B,stroke-width:2px,color:#1A1A2E;
    classDef student fill:#D1FAE5,stroke:#10B981,stroke-width:2px,color:#1A1A2E;
    classDef staff fill:#EFF6FF,stroke:#3B82F6,stroke-width:2px,color:#1A1A2E;
    classDef admin fill:#FEE2E2,stroke:#EF4444,stroke-width:2px,color:#1A1A2E;

    %% Nodes Definitions
    A([1. Application Launch]):::startEnd --> B[2. DataManager loads users, resources, slots, & alerts from local files]:::process
    B --> C{3. Remembered User ID exists?}:::decision
    
    C -- Yes --> D[4. Pre-fill Login Screen ID field]:::process
    C -- No --> E[5. Display Login Screen]:::process
    D --> E
    
    E --> F{6. Login Action}:::decision
    F -- Create Account --> G[7. Register User in SignUpScreen <br> sets isApproved = false]:::process
    G --> H[8. DataManager saves user CSV <br> broadcast Admin alert]:::process
    H --> E
    
    F -- Authenticate --> I{7. Valid ID & Password?}:::decision
    I -- No --> J[8. Visual shake transition <br> error red warning label]:::process
    J --> E
    
    I -- Yes --> K{8. Account Approved?}:::decision
    K -- No --> L[9. Warning: Account pending approval]:::process
    L --> E
    
    K -- Yes --> M{9. Route by Role}:::decision
    
    %% Student Path
    M -- Student --> ST_Dash[Student Dashboard]:::student
    ST_Dash --> ST1[Browse Sorted Approved Slots]:::student
    ST1 --> ST2{Seat Available?}:::decision
    ST2 -- Yes --> ST3[Book Seat in confirmedStudents]:::student
    ST2 -- No --> ST4[Join Waitlist Queue]:::student
    ST3 --> ST5[Check-in Attendance or Cancel booking]:::student
    ST4 --> ST5
    ST5 --> ST_Save[DataManager saves timeslot database]:::process
    
    %% Staff Path
    M -- Academic Staff --> SF_Dash[Faculty Portal]:::staff
    SF_Dash --> SF1[Select Target Room Catalog]:::staff
    SF1 --> SF2[Paint Weekly Calendar Grid via Click-and-Drag]:::staff
    SF2 --> SF3{Grid Collision?}:::decision
    SF3 -- Yes --> SF4[Paint Red: Block creation]:::staff
    SF3 -- No --> SF5[Propose Slot as PENDING]:::staff
    SF5 --> SF_Save[DataManager saves timeslot database]:::process
    
    %% Admin Path
    M -- Administrator --> AD_Dash[Admin Dashboard]:::admin
    AD_Dash --> AD1[Room Management: Create Rooms with Seating Capacity]:::admin
    AD_Dash --> AD2[Schedule Approvals: Approve/Decline Pending Slots]:::admin
    AD_Dash --> AD3[Account Requests: Approve/Decline Pending Registrations]:::admin
    AD1 --> AD_Save[DataManager saves system databases]:::process
    AD2 --> AD_Save
    AD3 --> AD_Save
    
    %% Endings
    ST_Save --> Z([Exit Application]):::startEnd
    SF_Save --> Z
    AD_Save --> Z
```

---

## II. Entity State Lifecycle Diagrams

### State Diagram A: User Account Lifecycle
This diagram illustrates the state transitions a user account undergoes from sign-up through administrative validation to active system usage:

```mermaid
stateDiagram-v2
    [*] --> Guest : Launches Application
    
    state Guest {
        [*] --> LoginView
        LoginView --> SignUpView : Click "Create an Account"
        SignUpView --> LoginView : Click "Back to Login"
    }

    state SignUpView {
        [*] --> FillRegistrationForm
        FillRegistrationForm --> PendingApproval : Click "Sign Up"
    }

    state PendingApproval {
        [*] --> SavedToUsersCSV : DataManager.saveUsers()
        SavedToUsersCSV --> NotifyAdmins : broadcastNotification()
    }

    PendingApproval --> Rejected : Admin clicks "Decline"
    Rejected --> [*] : Removed from database

    PendingApproval --> Active : Admin clicks "Approve" (isApproved = true)
    
    state Active {
        [*] --> SendWelcomeNotification : welcome notification generated
        SendWelcomeNotification --> AuthenticatedSession
        AuthenticatedSession --> StudentDashboard : Student logs in
        AuthenticatedSession --> StaffDashboard : Faculty logs in
        AuthenticatedSession --> AdminDashboard : Admin logs in
    }

    Active --> [*] : Log Out / Session terminated
```

### State Diagram B: Timeslot Scheduling Lifecycle
This diagram tracks a scheduling timeslot block from creation by faculty, through validation, booking, and final resolution (check-in or no-show cancellation):

```mermaid
stateDiagram-v2
    [*] --> Proposal : Painted by Academic Staff in Weekly Grid
    
    state Proposal {
        [*] --> PendingApproval : Created with status 'PENDING'
        PendingApproval --> NotifyAdmins : Admin alerts triggered
    }

    PendingApproval --> Declined : Admin clicks "Decline"
    Declined --> [*] : Status set to 'DECLINED' (notifies creator)

    PendingApproval --> Approved : Admin clicks "Approve"
    
    state Approved {
        [*] --> VisibleToStudents : Appends to sorted lists (CustomBST)
        VisibleToStudents --> OpenForBooking : Slots booked < Capacity
        
        state OpenForBooking {
            [*] --> BookedSeat : Student books (confirmedStudents.add)
        }
        
        OpenForBooking --> Full : Slots booked == Capacity
        
        state Full {
            [*] --> Waitlisted : New student added to FIFO Queue
        }
    }

    Approved --> ActiveSession : Scheduled Date/Time arrives
    
    state ActiveSession {
        [*] --> SessionStart
        SessionStart --> CheckedIn : Student clicks "Check-In" within 15 mins
        SessionStart --> GracePeriodRunning : 15-minute delay active
        GracePeriodRunning --> Cancelled : processNoShows() triggered (15 mins expire)
    }

    Cancelled --> PromoteWaitlist : cancelBooking() executed
    PromoteWaitlist --> ActiveSession : Next student promoted from Queue
    
    CheckedIn --> Completed : Meeting finishes
    Completed --> [*]
```
