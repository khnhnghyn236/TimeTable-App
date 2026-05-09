package main;

import javafx.application.Application;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import gui.*;
import users.*;
import scheduling.*;
import datastructures.*;
import java.time.*;
import java.util.*;

public class AppSchedulerGUI extends Application {
	private Stage window;

	public List<TimeSlot> systemTimeSlots = new ArrayList<>();
	public ObservableList<String> uiSlotList = FXCollections.observableArrayList();
	public Resource activeResource = new Resource("General Office", 2);

	// User Databases
	public CustomHashMap<String, User> userDatabase = new CustomHashMap<>();
	public ObservableList<User> pendingUsers = FXCollections.observableArrayList(); // For the Admin to approve

	public Student currentStudent;
	public AcademicStaff currentStaff;
	public Administrator currentAdmin;

	public LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

	@Override
	public void start(Stage primaryStage) {
		this.window = primaryStage;
		window.setTitle("TimeTable - Appointment Scheduling Platform");

		// 1. Populate Hash Map Database with NEW constructors (ID, Name, Email,
		// Password)
		Student testStudent = new Student("V202502059", "Truong Ba Ky", "ky@vinuni.edu", "pass123");
		testStudent.setApproved(true); // Pre-approve test user
		userDatabase.put("V202502059", testStudent);

		AcademicStaff testStaff = new AcademicStaff("P001", "Prof. Bob", "bob@vinuni.edu", "pass123");
		testStaff.setApproved(true); // Pre-approve test user
		userDatabase.put("P001", testStaff);

		Administrator testAdmin = new Administrator("A001", "Admin Alice", "alice@vinuni.edu", "admin123");
		userDatabase.put("A001", testAdmin);

		// --- Load Persisted Data ---
		systemTimeSlots = DataManager.loadState();
		updateStudentUIList();

		showLogin();
		window.show();
	}

	public boolean confirmAction(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK;
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

	public void showSignUp() {
		switchScreen(new Scene(new SignUpScreen(this).getContent(), 450, 500));
	}

	public static void main(String[] args) {
		launch(args);
	}
}