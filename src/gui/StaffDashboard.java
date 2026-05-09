package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import main.AppSchedulerGUI;
import scheduling.TimeSlot;
import users.Student;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class StaffDashboard {
	private AppSchedulerGUI app;
	private final boolean[] isAddingMode = { true };

	// Elevated to class-level so they can be refreshed dynamically
	private TabPane tabPane;
	private ListView<String> bookingList;
	private Tab gridTab;
	private Tab manageTab;

	public StaffDashboard(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #FFFFFF;");

		Label title = new Label("Faculty Hub - " + app.currentStaff.getName());
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		Button logoutBtn = new Button("Log Out");
		logoutBtn.setStyle("-fx-cursor: hand;");
		logoutBtn.setOnAction(e -> {
			if (app.confirmAction("Confirm Log Out", "Are you sure you want to log out?")) {
				app.showLogin();
			}
		});

		tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		manageTab = new Tab("View Appointments");
		manageTab.setContent(createManageTab());

		// --- BUG FIX: Instant UI Updates ---
		// This listener forces the list to refresh from the backend
		// every single time you click the "View Appointments" tab!
		manageTab.setOnSelectionChanged(e -> {
			if (manageTab.isSelected()) {
				updateManageList();
			}
		});

		Tab notifTab = new Tab("Notifications");
		notifTab.setContent(createNotificationsTab());

		gridTab = new Tab("Manage Availability (Booking Grid)");
		gridTab.setContent(createGridTab());

		tabPane.getTabs().addAll(manageTab, notifTab, gridTab);
		VBox.setVgrow(tabPane, Priority.ALWAYS);

		layout.getChildren().addAll(title, tabPane, logoutBtn);
		return layout;
	}

	// WINDOW 1: View Appointments
	private VBox createManageTab() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(15));

		bookingList = new ListView<>();
		bookingList.setPlaceholder(new Label("You have not created any timeslots yet."));

		updateManageList(); // Initial population

		// --- DOUBLE CLICK LOGIC ---
		bookingList.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				String selected = bookingList.getSelectionModel().getSelectedItem();
				if (selected != null) {
					showEditDialog(selected);
				}
			}
		});

		Label hint = new Label("Hint: Double-click a timeslot to edit or delete it.");
		hint.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

		box.getChildren().addAll(new Label("Your Active Timeslots:"), bookingList, hint);
		return box;
	}

	// Helper: Merges continuous blocks
	private void updateManageList() {
		ObservableList<String> activeBlocks = FXCollections.observableArrayList();
		if (app.systemTimeSlots.isEmpty()) {
			bookingList.setItems(activeBlocks);
			return;
		}

		app.systemTimeSlots.sort(Comparator.comparing(TimeSlot::getTimeRange));

		String currentDate = null;
		String currentStart = null;
		String currentEnd = null;
		Set<String> currentStudents = new HashSet<>();

		for (TimeSlot slot : app.systemTimeSlots) {
			String[] parts = slot.getTimeRange().split(" \\| ");
			String datePart = parts[0];
			String[] times = parts[1].split(" - ");
			String start = times[0];
			String end = times[1];

			if (currentDate == null) {
				currentDate = datePart;
				currentStart = start;
				currentEnd = end;
				addStudentsToSet(slot, currentStudents);
			} else if (currentDate.equals(datePart) && currentEnd.equals(start)) {
				currentEnd = end;
				addStudentsToSet(slot, currentStudents);
			} else {
				activeBlocks.add(formatBlock(currentDate, currentStart, currentEnd, currentStudents));
				currentDate = datePart;
				currentStart = start;
				currentEnd = end;
				currentStudents.clear();
				addStudentsToSet(slot, currentStudents);
			}
		}
		if (currentDate != null) {
			activeBlocks.add(formatBlock(currentDate, currentStart, currentEnd, currentStudents));
		}
		bookingList.setItems(activeBlocks);
	}

	private void addStudentsToSet(TimeSlot slot, Set<String> set) {
		if (slot.getConfirmedStudents() != null) {
			for (Student s : slot.getConfirmedStudents()) {
				set.add(s.getName());
			}
		}
	}

	private String formatBlock(String date, String start, String end, Set<String> students) {
		String base = date + " | " + start + " - " + end;
		if (students.isEmpty()) {
			return base + " | (Empty)";
		} else {
			return base + " | Booked by: " + String.join(", ", students);
		}
	}

	// --- NEW: Edit/Delete Dialog Window ---
	private void showEditDialog(String blockString) {
		Stage dialog = new Stage();
		dialog.setTitle("Manage Booking Block");

		VBox box = new VBox(15);
		box.setPadding(new Insets(20));

		String[] mainParts = blockString.split(" \\| ");
		String date = mainParts[0];
		String times = mainParts[1];
		String[] timeParts = times.split(" - ");
		String start = timeParts[0];
		String end = timeParts[1];

		Label info = new Label("Block Details:\nDate: " + date + "\nTime: " + start + " to " + end);
		info.setStyle("-fx-font-weight: bold;");

		Label students = new Label(mainParts.length > 2 ? mainParts[2] : "Currently Empty");

		// --- NEW: Edit Button Logic ---
		Button editBtn = new Button("Edit on Grid");
		editBtn.setStyle("-fx-background-color: #fbbc05; -fx-text-fill: black; -fx-cursor: hand;");
		editBtn.setOnAction(e -> {
			// Calculate the exact week of this booking
			LocalDate targetDate = LocalDate.parse(date);
			app.currentWeekStart = targetDate.with(DayOfWeek.MONDAY);

			// Refresh the grid tab behind the scenes
			gridTab.setContent(createGridTab());

			// Teleport the user to the Grid Tab (Index 2)
			tabPane.getSelectionModel().select(2);
			dialog.close();
		});

		Button deleteBtn = new Button("Delete Block");
		deleteBtn.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white; -fx-cursor: hand;");
		deleteBtn.setOnAction(e -> {
			if (app.confirmAction("Confirm Deletion",
					"Are you sure you want to delete this entire continuous block?")) {

				app.systemTimeSlots.removeIf(slot -> {
					String[] sParts = slot.getTimeRange().split(" \\| ");
					if (!sParts[0].equals(date))
						return false;
					String[] sTimes = sParts[1].split(" - ");
					return sTimes[0].compareTo(start) >= 0 && sTimes[1].compareTo(end) <= 0;
				});

				main.DataManager.saveState(app.systemTimeSlots);
				app.updateStudentUIList();
				updateManageList(); // Instantly updates the list
				gridTab.setContent(createGridTab()); // Instantly updates the background grid
				dialog.close();
			}
		});

		Button cancelBtn = new Button("Cancel (Close)");
		cancelBtn.setStyle("-fx-cursor: hand;");
		cancelBtn.setOnAction(e -> dialog.close());

		HBox buttons = new HBox(10, editBtn, deleteBtn, cancelBtn);
		box.getChildren().addAll(info, students, new Separator(), new Label("Choose an action:"), buttons);

		dialog.setScene(new Scene(box, 450, 250));
		dialog.show();
	}

	// WINDOW 2: Notifications
	private VBox createNotificationsTab() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(15));

		ObservableList<String> notifications = FXCollections.observableArrayList();
		if (app.currentStaff.isApproved()) {
			notifications.add("Welcome to TimeTable! Your Faculty account was approved by the Administrator.");
			notifications.add("Tip: Use the 'Manage Availability' tab to open timeslots for students.");
		}

		ListView<String> notifList = new ListView<>(notifications);
		box.getChildren().addAll(new Label("Your Messages:"), notifList);
		return box;
	}

	// WINDOW 3: Manage Availability (Grid)
	private VBox createGridTab() {
		VBox box = new VBox(15);
		box.setPadding(new Insets(15));
		box.setAlignment(Pos.CENTER);

		DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.US);
		Label weekLabel = new Label("Week of: " + app.currentWeekStart.format(weekFormatter));

		Button prev = new Button("< Prev Week");
		Button next = new Button("Next Week >");

		prev.setOnAction(e -> {
			app.currentWeekStart = app.currentWeekStart.minusWeeks(1);
			gridTab.setContent(createGridTab()); // Dynamic refresh without screen flickering
		});
		next.setOnAction(e -> {
			app.currentWeekStart = app.currentWeekStart.plusWeeks(1);
			gridTab.setContent(createGridTab());
		});

		HBox nav = new HBox(15, prev, weekLabel, next);
		nav.setAlignment(Pos.CENTER);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setGridLinesVisible(true);
		setupGrid(grid);

		ScrollPane scroll = new ScrollPane(grid);
		scroll.setFitToWidth(true);
		scroll.setPrefHeight(400);

		box.getChildren().addAll(nav, scroll);
		return box;
	}

	private void setupGrid(GridPane grid) {
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.US);
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.US);

		for (int day = 0; day < 7; day++) {
			LocalDate date = app.currentWeekStart.plusDays(day);
			Label dayName = new Label(date.format(dayFormatter));
			dayName.setStyle("-fx-font-weight: bold; -fx-text-fill: #5f6368;");
			Label dateNum = new Label(date.format(dateFormatter));
			dateNum.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a73e8;");

			VBox headerBox = new VBox(2, dayName, dateNum);
			headerBox.setAlignment(Pos.CENTER);
			headerBox.setPadding(new Insets(10));
			grid.add(headerBox, day + 1, 0);
		}

		LocalTime time = LocalTime.of(7, 0);
		for (int row = 0; row < 22; row++) {
			grid.add(new Label(" " + time.toString() + " "), 0, row + 1);
			for (int col = 0; col < 7; col++) {
				Button btn = createSlotButton(app.currentWeekStart.plusDays(col), time);
				grid.add(btn, col + 1, row + 1);
			}
			time = time.plusMinutes(30);
		}
	}

	private Button createSlotButton(LocalDate date, LocalTime time) {
		Button btn = new Button();
		btn.setPrefSize(110, 45);
		String range = date + " | " + time + " - " + time.plusMinutes(30);
		updateButtonStyle(btn, range);

		btn.setOnDragDetected(e -> {
			btn.startFullDrag();
			isAddingMode[0] = btn.getText().equals("");
			toggle(btn, range);
		});
		btn.setOnMouseDragEntered(e -> toggle(btn, range));
		btn.setOnAction(e -> {
			isAddingMode[0] = btn.getText().equals("");
			toggle(btn, range);
		});
		return btn;
	}

	private void toggle(Button btn, String range) {
		if (isAddingMode[0] && btn.getText().equals("")) {
			app.systemTimeSlots.add(new TimeSlot(range, app.activeResource));
		} else if (!isAddingMode[0] && btn.getText().equals("Booked")) {
			app.systemTimeSlots.removeIf(s -> s.getTimeRange().equals(range));
		}

		app.updateStudentUIList();
		main.DataManager.saveState(app.systemTimeSlots);
		updateButtonStyle(btn, range);
	}

	private void updateButtonStyle(Button btn, String range) {
		boolean exists = app.systemTimeSlots.stream().anyMatch(s -> s.getTimeRange().equals(range));
		btn.setText(exists ? "Booked" : "");
		btn.setStyle(exists ? "-fx-background-color: #e8f0fe; -fx-text-fill: #1a73e8; -fx-border-color: #1a73e8;"
				: "-fx-background-color: transparent; -fx-border-color: #dadce0;");
	}
}