package gui;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import main.AppSchedulerGUI;
import scheduling.TimeSlot;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class StaffDashboard {
	private AppSchedulerGUI app;
	private final boolean[] isAddingMode = { true };

	public StaffDashboard(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(20);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER); // Center aligned layout
		layout.setStyle("-fx-background-color: #FFFFFF;");

		Label title = new Label("Staff Dashboard - " + app.currentStaff.getName());
		title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

		// Force English for the week label [cite: 58]
		DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.US);
		Label weekLabel = new Label("Week of: " + app.currentWeekStart.format(weekFormatter));

		Button prev = new Button("< Prev Week");
		Button next = new Button("Next Week >");
		prev.setOnAction(e -> {
			app.currentWeekStart = app.currentWeekStart.minusWeeks(1);
			app.showStaff();
		});
		next.setOnAction(e -> {
			app.currentWeekStart = app.currentWeekStart.plusWeeks(1);
			app.showStaff();
		});

		HBox nav = new HBox(15, prev, weekLabel, next);
		nav.setAlignment(Pos.CENTER);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER); // Center aligned grid [cite: 31]
		grid.setGridLinesVisible(true);
		setupGrid(grid);

		ScrollPane scroll = new ScrollPane(grid);
		scroll.setFitToWidth(true);
		scroll.setPrefHeight(500);

		Button logout = new Button("Log Out");
		logout.setOnAction(e -> app.showLogin());

		layout.getChildren().addAll(title, nav, scroll, logout);
		return layout;
	}

	private void setupGrid(GridPane grid) {
		// Headers: Day Name (English) stacked ABOVE the Date
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

		// Time Slots: 7:00 AM - 6:00 PM [cite: 57]
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

		// Click and Drag for painting/erasing availability [cite: 57]
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
		updateButtonStyle(btn, range);
	}

	private void updateButtonStyle(Button btn, String range) {
		boolean exists = app.systemTimeSlots.stream().anyMatch(s -> s.getTimeRange().equals(range));
		btn.setText(exists ? "Booked" : "");
		btn.setStyle(exists ? "-fx-background-color: #e8f0fe; -fx-text-fill: #1a73e8; -fx-border-color: #1a73e8;"
				: "-fx-background-color: transparent; -fx-border-color: #dadce0;");
	}
}