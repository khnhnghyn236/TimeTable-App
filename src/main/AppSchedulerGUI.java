package main;

import javafx.application.Application;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import gui.*;
import users.*;
import scheduling.*;
import datastructures.*; // Make sure this is imported!
import java.time.*;
import java.util.*;

public class AppSchedulerGUI extends Application {
	private Stage window;

	public List<TimeSlot> systemTimeSlots = new ArrayList<>();
	public ObservableList<String> uiSlotList = FXCollections.observableArrayList();
	public Resource activeResource = new Resource("General Office", 2);

	// --- TASK T6: Custom Hash Map for O(1) User Lookups ---
	public CustomHashMap<String, User> userDatabase = new CustomHashMap<>();

	public Student currentStudent;
	public AcademicStaff currentStaff;
	public Administrator currentAdmin;

	public LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

	@Override
	public void start(Stage primaryStage) {
		this.window = primaryStage;
		window.setTitle("TimeTable - Appointment Scheduling Platform");

		// Populate Hash Map Database
		userDatabase.put("V202502059", new Student("V202502059", "Truong Ba Ky"));
		userDatabase.put("P001", new AcademicStaff("P001", "Prof. Bob"));
		userDatabase.put("A001", new Administrator("A001", "Admin Alice"));

		// Fast O(1) Lookups to set current session users
		currentStudent = (Student) userDatabase.get("V202502059");
		currentStaff = (AcademicStaff) userDatabase.get("P001");
		currentAdmin = (Administrator) userDatabase.get("A001");

		// Load Persisted Data on Startup
		systemTimeSlots = DataManager.loadState();
		updateStudentUIList();

		showLogin();
		window.show();
	}

	public void updateStudentUIList() {
		uiSlotList.clear();
		if (systemTimeSlots.isEmpty())
			return;

		// Custom BST Sorting Algorithm
		CustomBST<TimeSlot> bst = new CustomBST<>();
		for (TimeSlot slot : systemTimeSlots) {
			bst.insert(slot); // O(log n) insertion
		}

		// Retrieve chronologically sorted list using In-Order Traversal
		List<TimeSlot> sortedSlots = bst.getSortedList();

		String currentDate = null;
		String currentStart = null;
		String currentEnd = null;

		for (TimeSlot slot : sortedSlots) {
			String[] parts = slot.getTimeRange().split(" \\| ");
			String datePart = parts[0];
			String[] times = parts[1].split(" - ");
			String start = times[0];
			String end = times[1];

			if (currentDate == null) {
				currentDate = datePart;
				currentStart = start;
				currentEnd = end;
			} else if (currentDate.equals(datePart) && currentEnd.equals(start)) {
				currentEnd = end;
			} else {
				uiSlotList.add(currentDate + " | " + currentStart + " - " + currentEnd);
				currentDate = datePart;
				currentStart = start;
				currentEnd = end;
			}
		}

		if (currentDate != null) {
			uiSlotList.add(currentDate + " | " + currentStart + " - " + currentEnd);
		}
	}

	public void switchScreen(Scene scene) {
		window.setScene(scene);
		window.setWidth(Double.NaN);
		window.setHeight(Double.NaN);
		window.sizeToScene();
		window.centerOnScreen();
	}

	public void showLogin() {
		switchScreen(new Scene(new LoginScreen(this).getContent(), 450, 400));
	}

	public void showStudent() {
		switchScreen(new Scene(new StudentDashboard(this).getContent(), 500, 500));
	}

	public void showAdmin() {
		switchScreen(new Scene(new AdminDashboard(this).getContent(), 450, 400));
	}

	public void showStaff() {
		switchScreen(new Scene(new StaffDashboard(this).getContent(), 950, 750));
	}

	public static void main(String[] args) {
		launch(args);
	}
}