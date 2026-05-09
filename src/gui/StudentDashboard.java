package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
//import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import main.AppSchedulerGUI;
import scheduling.TimeSlot;

public class StudentDashboard {
	private AppSchedulerGUI app;
	private TabPane tabPane; // We need this at the class level to switch tabs via code

	public StudentDashboard(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #FFFFFF;");

		Label title = new Label("Student Hub - " + app.currentStudent.getName());
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		// --- NEW: Double-click to verify Log Out ---
		Button logoutBtn = new Button("Log Out");
		logoutBtn.setStyle("-fx-cursor: hand;");
		logoutBtn.setOnAction(e -> {
			if (app.confirmAction("Confirm Log Out", "Are you sure you want to log out?")) {
				app.showLogin();
			}
		});

		// --- NEW: The 3-Window Setup ---
		tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		Tab bookTab = new Tab("Book Appointments");
		bookTab.setContent(createBookingTab());

		Tab notifTab = new Tab("Notifications");
		notifTab.setContent(createNotificationsTab());

		Tab manageTab = new Tab("Manage Schedules");
		manageTab.setContent(createManageTab());

		tabPane.getTabs().addAll(bookTab, notifTab, manageTab);
		VBox.setVgrow(tabPane, Priority.ALWAYS); // Fill the screen

		layout.getChildren().addAll(title, tabPane, logoutBtn);
		return layout;
	}

	// WINDOW 1: Booking Timeslots
	private VBox createBookingTab() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(15));

		ListView<String> list = new ListView<>(app.uiSlotList);
		list.setPlaceholder(new Label("No available slots provided by staff."));

		Button bookBtn = new Button("Book Selected Slot");
		bookBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-cursor: hand;");

		bookBtn.setOnAction(e -> {
			String selected = list.getSelectionModel().getSelectedItem();
			if (selected != null) {
				app.systemTimeSlots.stream().filter(s -> s.getTimeRange().equals(selected)).findFirst().ifPresent(s -> {
					s.bookSlot(app.currentStudent);
					main.DataManager.saveState(app.systemTimeSlots); // Save change

					// Show simple alert so they know it worked
					Alert success = new Alert(Alert.AlertType.INFORMATION, "Slot booked successfully!");
					success.setHeaderText(null);
					success.show();
				});
			}
		});

		box.getChildren().addAll(new Label("Available Appointment Blocks:"), list, bookBtn);
		return box;
	}

	// WINDOW 2: Notifications
	private VBox createNotificationsTab() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(15));

		ObservableList<String> notifications = FXCollections.observableArrayList();

		// If they are logged in, it means the Admin approved them!
		if (app.currentStudent.isApproved()) {
			notifications.add("Welcome to TimeTable! Your account request was approved by the Administrator.");
			notifications.add("Tip: You can view and manage your booked slots in the 'Manage Schedules' tab.");
		}

		ListView<String> notifList = new ListView<>(notifications);
		box.getChildren().addAll(new Label("Your Messages:"), notifList);
		return box;
	}

	// WINDOW 3: Manage Booked Schedules
	private VBox createManageTab() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(15));

		ObservableList<String> myBookings = FXCollections.observableArrayList();

		// Find all slots where this student is in the confirmed list
		for (TimeSlot slot : app.systemTimeSlots) {
			if (slot.getConfirmedStudents() != null && slot.getConfirmedStudents().contains(app.currentStudent)) {
				myBookings.add(slot.getTimeRange());
			}
		}

		ListView<String> bookingList = new ListView<>(myBookings);
		bookingList.setPlaceholder(new Label("You have no booked appointments."));

		Button editBtn = new Button("Edit Booking");
		Button cancelBtn = new Button("Cancel Booking");
		cancelBtn.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white; -fx-cursor: hand;");

		// EDIT: Redirects user back to the Booking tab (Index 0)
		editBtn.setOnAction(e -> {
			tabPane.getSelectionModel().select(0);
		});

		// CANCEL: Requires double-click verification
		cancelBtn.setOnAction(e -> {
			String selected = bookingList.getSelectionModel().getSelectedItem();
			if (selected != null) {
				if (app.confirmAction("Confirm Cancellation",
						"Are you sure you want to cancel this booking? The slot will be given to the next person on the waitlist.")) {

					app.systemTimeSlots.stream().filter(s -> s.getTimeRange().equals(selected)).findFirst()
							.ifPresent(s -> {
								s.cancelBooking(app.currentStudent);
								main.DataManager.saveState(app.systemTimeSlots); // Save change
								myBookings.remove(selected); // Remove from view
							});
				}
			}
		});

		HBox buttonBox = new HBox(10, editBtn, cancelBtn);
		box.getChildren().addAll(new Label("Your Confirmed Appointments:"), bookingList, buttonBox);
		return box;
	}
}