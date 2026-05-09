package scheduling;

import users.Student; // We have to import this because Student is in a different package!

public class Appointment {
	private Student student;
	private TimeSlot timeSlot;

	public Appointment(Student student, TimeSlot timeSlot) {
		this.student = student;
		this.timeSlot = timeSlot;
	}
}