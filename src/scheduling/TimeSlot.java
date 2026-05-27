package scheduling;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;
import users.Student;

public class TimeSlot implements Comparable<TimeSlot> {
    private String timeRange;
    private Resource resource;
    private String creatorId;
    private String creatorName;
    private String status;
    private String title;
    private int slotCapacity;

    private LocalDate localDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Tracking lists
    private List<Student> confirmedStudents;
    private List<Student> checkedInStudents;
    private Queue<Student> waitlist;

    // Legacy constructor
    public TimeSlot(String timeRange, Resource resource) {
        this(timeRange, resource, "ADMIN", "Administrator", "APPROVED", "Meeting", resource.getCapacity());
    }

    // New constructor used by StaffDashboard
    public TimeSlot(String timeRange, Resource resource, String creatorId, String creatorName, String status,
            String title, int slotCapacity) {
        this.timeRange = timeRange;
        this.resource = resource;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.status = status;
        this.title = title;
        this.slotCapacity = slotCapacity;
        this.confirmedStudents = new ArrayList<>();
        this.checkedInStudents = new ArrayList<>();
        this.waitlist = new LinkedList<>();
        parseTimeRange();
    }

    private void parseTimeRange() {
        try {
            // Handle both "2026-05-26 | 07:30 - 10:30" and "2026-05-27 07:20 - 12:10"
            String[] parts = timeRange.split("\\s*\\|\\s*|\\s{2,}");
            if (parts.length >= 2) {
                this.localDate = LocalDate.parse(parts[0].trim());
                String[] timeParts = parts[1].split("\\s*-\\s*");
                if (timeParts.length == 2) {
                    this.startTime = LocalTime.parse(timeParts[0].trim());
                    this.endTime = LocalTime.parse(timeParts[1].trim());
                    return;
                }
            }
        } catch (Exception e) {
            // Ignore parse errors, handle fallback below
        }

        // Fallback to prevent NullPointerException
        if (this.localDate == null) {
            this.localDate = LocalDate.now();
            this.startTime = LocalTime.of(8, 0);
            this.endTime = LocalTime.of(9, 0);
        }
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        parseTimeRange();
    }

    public String getTimeRange() {
        return timeRange;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSlotCapacity() {
        return slotCapacity;
    }

    public void setSlotCapacity(int slotCapacity) {
        this.slotCapacity = slotCapacity;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean overlaps(LocalDate d, LocalTime s, LocalTime e) {
        if (!this.localDate.equals(d))
            return false;
        return this.startTime.isBefore(e) && this.endTime.isAfter(s);
    }

    public String getSummary() {
        return timeRange + " | " + title + " @ " + resource.getName() + " (" + confirmedStudents.size() + "/"
                + slotCapacity + ")";
    }

    public void bookSlot(Student student) {
        if (confirmedStudents.size() < slotCapacity) {
            confirmedStudents.add(student);
            System.out.println("SUCCESS: " + student.getName() + " booked " + timeRange);
        } else {
            waitlist.add(student);
            System.out.println("FULL: " + student.getName() + " placed on waitlist.");
        }
    }

    public void addConfirmedStudent(Student student) {
        confirmedStudents.add(student);
    }

    public void addWaitlistedStudent(Student student) {
        waitlist.add(student);
    }

    public Queue<Student> getWaitlist() {
        return waitlist;
    }

    public boolean checkIn(Student student) {
        if (confirmedStudents.contains(student) && !checkedInStudents.contains(student)) {
            checkedInStudents.add(student);
            System.out.println("CHECK-IN SUCCESS: " + student.getName() + " has arrived.");
            return true;
        }
        return false;
    }

    public void processNoShows() {
        System.out.println("\n--- 15 MINUTE GRACE PERIOD EXPIRED ---");
        List<Student> toCancel = new ArrayList<>();

        for (Student student : confirmedStudents) {
            if (!checkedInStudents.contains(student)) {
                System.out.println("NO-SHOW DETECTED: " + student.getName());
                toCancel.add(student);
            }
        }

        for (Student noShow : toCancel) {
            cancelBooking(noShow);
        }
    }

    /**
     * Cancels the booking for the given student.
     * If a waitlisted student is promoted, that student is returned so the caller
     * can send them a notification. Returns null if no promotion occurred.
     */
    public Student cancelBooking(Student student) {
        if (confirmedStudents.remove(student)) {
            System.out.println("CANCELLED: " + student.getName() + " lost their slot.");
            if (!waitlist.isEmpty()) {
                Student nextInLine = waitlist.poll();
                confirmedStudents.add(nextInLine);
                System.out.println("AUTO-ASSIGN: Slot given to waitlisted student -> " + nextInLine.getName());
                return nextInLine;
            }
        } else {
            // Student is on the waitlist — just remove them
            waitlist.remove(student);
            System.out.println("WAITLIST REMOVE: " + student.getName() + " left the waitlist.");
        }
        return null;
    }

    @Override
    public int compareTo(TimeSlot other) {
        return this.timeRange.compareTo(other.getTimeRange());
    }

    public List<Student> getConfirmedStudents() {
        return confirmedStudents;
    }
}