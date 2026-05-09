package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import main.AppSchedulerGUI;
import scheduling.TimeSlot;

public class StudentDashboard {
	private AppSchedulerGUI app;

	public StudentDashboard(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.TOP_CENTER);

		Label title = new Label("Student: " + app.currentStudent.getName());
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		ListView<String> list = new ListView<>(app.uiSlotList);
		list.setPlaceholder(new Label("No available slots provided by staff."));

		Button book = new Button("Book Appointment");
		Button checkIn = new Button("Check-In");
		Button logout = new Button("Log Out");

		book.setOnAction(e -> {
			String selected = list.getSelectionModel().getSelectedItem();
			app.systemTimeSlots.stream().filter(s -> s.getTimeRange().equals(selected)).findFirst()
					.ifPresent(s -> s.bookSlot(app.currentStudent));
		});

		logout.setOnAction(e -> app.showLogin());

		layout.getChildren().addAll(title, new Label("Available Appointment Blocks:"), list,
				new HBox(10, book, checkIn), logout);
		return layout;
	}
}