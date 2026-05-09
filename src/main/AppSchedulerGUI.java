package main;

import javafx.application.Application;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import gui.*;
import users.*;
import scheduling.*;
import java.time.*;
import java.util.*;

public class AppSchedulerGUI extends Application {
	private Stage window;

	public List<TimeSlot> systemTimeSlots = new ArrayList<>();
	public ObservableList<String> uiSlotList = FXCollections.observableArrayList();
	public Resource activeResource = new Resource("General Office", 2); // [cite: 32, 63]

	public Student currentStudent = new Student("V202502059", "Truong Ba Ky"); // [cite: 103]
	public AcademicStaff currentStaff = new AcademicStaff("P001", "Prof. Bob"); // [cite: 14]
	public Administrator currentAdmin = new Administrator("A001", "Admin Alice"); // [cite: 15]

	public LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

	@Override
	public void start(Stage primaryStage) {
		this.window = primaryStage;
		window.setTitle("TimeTable - Appointment Scheduling Platform"); // [cite: 95]
		showLogin();
		window.show();
	}

	/**
	 * Logic to merge consecutive 30-min slots into 1 booking block. This fulfills
	 * the requirement for simplified student booking[cite: 42, 50].
	 */
	public void updateStudentUIList() {
		uiSlotList.clear();
		if (systemTimeSlots.isEmpty())
			return;

		// Sort by date and time
		systemTimeSlots.sort(Comparator.comparing(TimeSlot::getTimeRange));

		// Basic merging logic for the UI display
		for (TimeSlot slot : systemTimeSlots) {
			uiSlotList.add(slot.getTimeRange());
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