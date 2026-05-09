# TimeTable - Appointment Scheduling Platform

[cite_start]TimeTable is a centralized Appointment Scheduling Platform tailored for university campus services. [cite: 12] [cite_start]It provides a unified interface for students, academic staff, and administrators to streamline the booking of office hours, advising sessions, and shared resources. [cite: 13] 

[cite_start]Built entirely in Java using the JavaFX framework, the project heavily applies Object-Oriented Programming (OOP) principles and custom data structures. [cite: 13]

## 🛠 Architecture & Technologies

* [cite_start]**Language:** Java [cite: 54]
* [cite_start]**GUI Framework:** JavaFX (Event-driven UI) [cite: 54]
* [cite_start]**Architecture:** Strict MVC (Model-View-Controller) pattern decoupling the JavaFX UI frontend from the underlying data management backend. [cite: 21]
* [cite_start]**Data Persistence:** Standard File I/O for local storage. [cite: 54]
* [cite_start]**Time Tracking:** `java.time` API for precise calendar mathematics. [cite: 55]

## 🚀 Core Features & Use Cases

[cite_start]The system is driven by three primary actors, each with specific roles and capabilities: [cite: 26]

### For Students
* [cite_start]View available TimeSlots dynamically merged into continuous sessions. [cite: 38]
* [cite_start]Book appointments or join a priority automated waitlist if a resource is at maximum capacity. [cite: 64]
* [cite_start]Utilize a simulated check-in feature. [cite: 65]

### For Academic Staff
* [cite_start]Access a dynamic, auto-generated 7-day calendar grid. [cite: 61]
* [cite_start]Define working hours and create continuous availability blocks using a seamless "click-and-drag" event handler to paint or erase slots. [cite: 61]
* [cite_start]Real-time synchronization instantly triggers UI updates on the Student Dashboard. [cite: 62]

### For Administrators
* [cite_start]Utilize a dedicated control panel to create new campus resources (e.g., study rooms, labs). [cite: 28, 66]
* [cite_start]Configure strict capacity limits for specific resources. [cite: 28, 36]

## 🧠 Algorithmic Logic & Data Structures

* [cite_start]**Waitlist Queue (FIFO):** Implemented for the automated waitlist system within the `TimeSlot` class. [cite: 47] [cite_start]If a student misses the 15-minute grace period, the `processNoShows()` method cancels their booking and calls `poll()` on the queue to automatically promote the next student to the confirmed list. [cite: 35, 44, 65]
* [cite_start]**Sorting & Merging Algorithms:** Utilizes `Comparator` sorting algorithms to group adjacent 30-minute availability blocks into single, continuous booking sessions for enhanced UX. [cite: 48]
* [cite_start]**Custom Structures:** The backend is designed to swap standard Java Collections with custom-built Hash Maps (for $O(1)$ authentication) and Binary Search Trees (for $O(\log n)$ schedule management). [cite: 53]

## 👥 Team Macau

[cite_start]This project was developed for the COMP1020 Object-Oriented Programming & Data Structures course (Spring 2026) at the College of Engineering and Computer Science, VinUniversity. [cite: 2, 3]

* [cite_start]**Truong Ba Ky** (Leader) - GUI development, MVC refactoring, JavaFX optimization [cite: 90, 103]
* [cite_start]**Nguyen Hong Khanh** - GUI development, MVC refactoring, JavaFX optimization [cite: 90, 103]
* [cite_start]**Nguyen Trong Nguyen** - Backend logic, slot merging algorithms, custom data structures [cite: 91, 103]
* [cite_start]**Tran Trong Tai** - Backend logic, slot merging algorithms, custom data structures [cite: 91, 103]
