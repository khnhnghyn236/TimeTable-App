# TimeTable - Appointment Scheduling Platform

[cite_start]TimeTable is a centralized Appointment Scheduling Platform tailored for university campus services.  

[cite_start]It provides a unified interface for students, academic staff, and administrators to streamline the booking of office hours, advising sessions, and shared resources. 

[cite_start]Built entirely in Java using the JavaFX framework, the project heavily applies Object-Oriented Programming (OOP) principles and custom data structures. 

## 🛠 Architecture & Technologies

* [cite_start]**Language:** Java 
* [cite_start]**GUI Framework:** JavaFX (Event-driven UI) 
* [cite_start]**Architecture:** Strict MVC (Model-View-Controller) pattern decoupling the JavaFX UI frontend from the underlying data management backend. 
* [cite_start]**Data Persistence:** Standard File I/O for local storage. 
* [cite_start]**Time Tracking:** `java.time` API for precise calendar mathematics. 

## 🚀 Core Features & Use Cases

[cite_start]The system is driven by three primary actors, each with specific roles and capabilities: 
### For Students
* [cite_start]View available TimeSlots dynamically merged into continuous sessions. 
* [cite_start]Book appointments or join a priority automated waitlist if a resource is at maximum capacity.
* [cite_start]Utilize a simulated check-in feature.
### For Academic Staff
* [cite_start]Access a dynamic, auto-generated 7-day calendar grid. 
* [cite_start]Define working hours and create continuous availability blocks using a seamless "click-and-drag" event handler to paint or erase slots. 
* [cite_start]Real-time synchronization instantly triggers UI updates on the Student Dashboard. 
### For Administrators
* [cite_start]Utilize a dedicated control panel to create new campus resources (e.g., study rooms, labs). 
* [cite_start]Configure strict capacity limits for specific resources.

## 🧠 Algorithmic Logic & Data Structures

* [cite_start]**Waitlist Queue (FIFO):** Implemented for the automated waitlist system within the `TimeSlot` class. [cite_start]If a student misses the 15-minute grace period, the `processNoShows()` method cancels their booking and calls `poll()` on the queue to automatically promote the next student to the confirmed list.
* [cite_start]**Sorting & Merging Algorithms:** Utilizes `Comparator` sorting algorithms to group adjacent 30-minute availability blocks into single, continuous booking sessions for enhanced UX.
* [cite_start]**Custom Structures:** The backend is designed to swap standard Java Collections with custom-built Hash Maps (for $O(1)$ authentication) and Binary Search Trees (for $O(\log n)$ schedule management). 

## 👥 Team Macau

[cite_start]This project was developed for the COMP1020 Object-Oriented Programming & Data Structures course (Spring 2026) at the College of Engineering and Computer Science, VinUniversity. 

* [cite_start]**Truong Ba Ky** 
* [cite_start]**Nguyen Hong Khanh**  
* [cite_start]**Nguyen Trong Nguyen**  
* [cite_start]**Tran Trong Tai** 
