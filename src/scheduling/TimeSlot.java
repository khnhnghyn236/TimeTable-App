package scheduling;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;
import users.Student;

public class TimeSlot implements Comparable<TimeSlot> {
	private String timeRange;
	private Resource resource;

	// Tracking lists
	private List<Student> confirmedStudents;
	private List<Student> checkedInStudents;
	private Queue<Student> waitlist;

	public TimeSlot(String timeRange, Resource resource) {
		this.timeRange = timeRange;
		this.resource = resource;
		this.confirmedStudents = new ArrayList<>();
		this.checkedInStudents = new ArrayList<>();
		this.waitlist = new LinkedList<>();
	}

	public String getTimeRange() {
		return timeRange;
	}

	public Resource getResource() {
		return resource;
	}

	public void bookSlot(Student student) {
		if (confirmedStudents.size() < resource.getCapacity()) {
			confirmedStudents.add(student);
			System.out.println("SUCCESS: " + student.getName() + " booked " + timeRange);
		} else {
			waitlist.add(student);
			System.out.println("FULL: " + student.getName() + " placed on waitlist.");
		}
	}

	// NEW: Check-in Logic
	public boolean checkIn(Student student) {
		if (confirmedStudents.contains(student) && !checkedInStudents.contains(student)) {
			checkedInStudents.add(student);
			System.out.println("CHECK-IN SUCCESS: " + student.getName() + " has arrived.");
			return true;
		}
		return false;
	}

	// NEW: No-Show Grace Period Logic (Feature 4.3.8)
	public void processNoShows() {
		System.out.println("\n--- 15 MINUTE GRACE PERIOD EXPIRED ---");
		// Create a copy of the list to avoid ConcurrentModificationException while
		// removing
		List<Student> toCancel = new ArrayList<>();

		for (Student student : confirmedStudents) {
			if (!checkedInStudents.contains(student)) {
				System.out.println("NO-SHOW DETECTED: " + student.getName());
				toCancel.add(student);
			}
		}

		// Cancel the no-shows and automatically assign to waitlist
		for (Student noShow : toCancel) {
			cancelBooking(noShow);
		}
	}

	public void cancelBooking(Student student) {
		if (confirmedStudents.remove(student)) {
			System.out.println("CANCELLED: " + student.getName() + " lost their slot.");
			if (!waitlist.isEmpty()) {
				Student nextInLine = waitlist.poll();
				confirmedStudents.add(nextInLine);
				System.out.println("AUTO-ASSIGN: Slot given to waitlisted student -> " + nextInLine.getName());
			}
		}
	}

	@Override
	public int compareTo(TimeSlot other) {
		// Since your timeRange strings are formatted chronologically
		// (e.g., "2026-04-29 | 10:00 - 10:30"), standard string comparison works
		// perfectly!
		return this.timeRange.compareTo(other.getTimeRange());
	}

	public List<Student> getConfirmedStudents() {
		return confirmedStudents;
	}
}