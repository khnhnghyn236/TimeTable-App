# TimeTable - Appointment Scheduling Platform

TimeTable is a centralized Appointment Scheduling Platform tailored for university campus services.  

It provides a unified interface for students, academic staff, and administrators to streamline the booking of office hours, advising sessions, and shared resources. 

Built entirely in Java using the JavaFX framework, the project heavily applies Object-Oriented Programming (OOP) principles and custom data structures. 

## 🛠 Architecture & Technologies

* **Language:** Java 
* **GUI Framework:** JavaFX (Event-driven UI) 
* **Architecture:** Strict MVC (Model-View-Controller) pattern decoupling the JavaFX UI frontend from the underlying data management backend. 
* **Data Persistence:** Standard File I/O for local storage. 
* **Time Tracking:** `java.time` API for precise calendar mathematics. 

## 🚀 Core Features & Use Cases

The system is driven by three primary actors, each with specific roles and capabilities: 
### For Students
* View available TimeSlots dynamically merged into continuous sessions. 
* Book appointments or join a priority automated waitlist if a resource is at maximum capacity.
* Utilize a simulated check-in feature.
### For Academic Staff
* Access a dynamic, auto-generated 7-day calendar grid. 
* Define working hours and create continuous availability blocks using a seamless "click-and-drag" event handler to paint or erase slots. 
* Real-time synchronization instantly triggers UI updates on the Student Dashboard. 
### For Administrators
* Utilize a dedicated control panel to create new campus resources (e.g., study rooms, labs). 
* Configure strict capacity limits for specific resources.

## 🧠 Algorithmic Logic & Data Structures

* **Waitlist Queue (FIFO):** Implemented for the automated waitlist system within the `TimeSlot` class. [cite_start]If a student misses the 15-minute grace period, the `processNoShows()` method cancels their booking and calls `poll()` on the queue to automatically promote the next student to the confirmed list.
* **Sorting & Merging Algorithms:** Utilizes `Comparator` sorting algorithms to group adjacent 30-minute availability blocks into single, continuous booking sessions for enhanced UX.
* **Custom Structures:** The backend is designed to swap standard Java Collections with custom-built Hash Maps (for $O(1)$ authentication) and Binary Search Trees (for $O(\log n)$ schedule management). 

## 👥 Team Macau

This project was developed for the COMP1020 Object-Oriented Programming & Data Structures course (Spring 2026) at the College of Engineering and Computer Science, VinUniversity. 

* [cite_start]**Truong Ba Ky** 
* [cite_start]**Nguyen Hong Khanh**  
* [cite_start]**Nguyen Trong Nguyen**  
* [cite_start]**Tran Trong Tai** 
