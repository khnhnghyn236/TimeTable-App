# Data Structures & Algorithmic Analysis

This document provides a rigorous technical analysis of the custom-built data structures and algorithms implemented in the TimeTable scheduling system, including asymptotic complexity profiles and engineering trade-offs.

## I. Custom Data Structures Analysis

### 1. `CustomHashMap<K, V>` (Hash Map with Separate Chaining)
*   **Implementation Mechanics**: Resolves hash collisions using **Separate Chaining**. It maintains an array of singly-linked list headers (`Entry<K, V>[] table`) with an initial capacity of 16 buckets. Hash calculation is done using `Math.abs(key.hashCode()) % table.length`. New entries are added to the head of the linked list chain to ensure $O(1)$ insertions.
*   **Asymptotic Complexity**:
    *   *Search / Retrieval*: Average case $O(1)$, Worst case $O(n)$ (when all keys map to the same bucket).
    *   *Insertion*: Average case $O(1)$, Worst case $O(n)$ (when updating a key in a long collision chain).
*   **Core Trade-offs**: Employs no dynamic resizing or load-factor rehashing. While it avoids execution spikes from memory re-allocation, it is vulnerable to degradation to $O(n)$ search times under extremely high key densities.

### 2. `CustomBST<T extends Comparable<T>>` (Binary Search Tree)
*   **Implementation Mechanics**: A generic Binary Search Tree for types implementing `Comparable`. Insertions utilize recursive subtree comparison (`insertRec()`). Sorting is achieved through a recursive **In-Order Traversal** (`inOrderRec()`), which dumps nodes sequentially (Left Subtree ➔ Root Node ➔ Right Subtree) into an array.
*   **Asymptotic Complexity**:
    *   *Search / Insertion*: Average case $O(\log n)$, Worst case $O(n)$ (when items are inserted in chronological order, forming a degenerate, unbalanced linear list).
    *   *In-Order Traversal Sorting*: $O(n)$ time complexity.
*   **Core Trade-offs**: Relies on recursive methods, which are simple and clean but vulnerable to `StackOverflowError` under very deep call depths. It lack self-balancing algorithms (such as AVL or Red-Black mechanisms), leaving it vulnerable to skewed structures if timeslots are loaded in strict sequential order.

### 3. FIFO Waitlist Queue (`java.util.Queue`)
*   **Implementation Mechanics**: Utilizes a `java.util.LinkedList` reference to implement a First-In-First-Out (FIFO) queue contract. The system locks timeslot seats; additions push students to the tail (`add()`), and cancellations poll the head (`poll()`) to auto-promote waitlisted students.
*   **Asymptotic Complexity**:
    *   *Enqueue (`add`)*: $O(1)$ constant time.
    *   *Dequeue (`poll`)*: $O(1)$ constant time.
*   **Core Trade-offs**: Employs double-linked nodes. This provides constant-time queue operations but introduces double-pointer memory overhead.

---

## II. Custom Data Structures and Complexity Summary

| Collection / Structure | Average-Case Insertion | Average-Case Search | Worst-Case Search | Primary Application |
| :--- | :--- | :--- | :--- | :--- |
| **`CustomHashMap`** | $O(1)$ | $O(1)$ | $O(n)$ | Fast user session validation |
| **`CustomBST`** | $O(\log n)$ | $O(\log n)$ | $O(n)$ | Sorted timeslot catalog aggregation |
| **`FIFO Queue`** | $O(1)$ | $O(1)$ | $O(1)$ | Priority student waitlist queue |

---

## III. Key Algorithms Analysis

### 1. Secure SHA-256 Password Cryptography
The password security utility hashes plain credentials prior to persistent saving:
*   **Implementation**: Utilizes `java.security.MessageDigest.getInstance("SHA-256")`. It processes raw credentials as UTF-8 bytes, computes a 256-bit digest, and formats the output into a 64-character hexadecimal representation.
*   **Complexity**: $O(m)$ time complexity, where $m$ represents password string length. Memory footprint is constant at $O(1)$ (always producing a 32-byte digest).

### 2. Timeslot Overlap Verification
To prevent scheduling resource double-bookings or concurrent faculty assignments:
*   **Formula**:
    $$\text{Overlap} \iff (\text{Date}_A = \text{Date}_B) \land (\text{Start}_A < \text{End}_B) \land (\text{End}_A > \text{Start}_B)$$
*   **Implementation**: Implemented as `overlaps(LocalDate d, LocalTime s, LocalTime e)` inside `TimeSlot.java`. Returns `true` if dates are identical and time spans intersect.
*   **Complexity**: $O(1)$ constant time execution.

### 3. Chronological Merging & Normalization
To group adjacent 30-minute availability proposal blocks into clean, continuous sessions:
*   **Implementation**: Implemented as `normalizeTimeSlots()` in `AppSchedulerGUI.java`. It operates as follows:
    1.  Sorts system timeslots using `Comparator.comparing` (sorting by Date, Room, Creator ID, and Start Time sequentially).
    2.  Iterates linearly through the sorted list, checking if the current timeslot matches the next timeslot's Date, Creator, Room, and Title, and if the current slot's End Time is equal to or after the next slot's Start Time.
    3.  If conditions are met, it merges the timespan (`current.setEndTime(next.getEndTime())`) and deletes the redundant block.
*   **Complexity**:
    *   *Sorting*: $O(n \log n)$ time complexity.
    *   *Merging*: $O(n)$ linear traversal.
    *   *Overall Time Complexity*: $O(n \log n)$.
    *   *Auxiliary Memory Complexity*: $O(n)$ to build the merged array.
