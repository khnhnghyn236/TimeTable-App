package gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import main.AppSchedulerGUI;
import scheduling.Resource;

public class AdminDashboard {
	private AppSchedulerGUI app;

	public AdminDashboard(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #FFFFFF;");

		Label titleLabel = new Label("Admin Dashboard - " + app.currentAdmin.getName());
		titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		// Input fields for Resource Creation [cite: 62]
		TextField resourceNameInput = new TextField();
		resourceNameInput.setPromptText("Enter Resource Name (e.g., Library Study Room B)");

		TextField capacityInput = new TextField();
		capacityInput.setPromptText("Enter Capacity (e.g., 4)");

		Button createBtn = new Button("Create Resource");
		Label statusLabel = new Label("Status: Ready");
		statusLabel.setStyle("-fx-text-fill: green;");

		createBtn.setOnAction(e -> {
			try {
				String name = resourceNameInput.getText();
				int capacity = Integer.parseInt(capacityInput.getText());

				// Updates the shared activeResource in the main App [cite: 63]
				app.activeResource = new Resource(name, capacity);
				statusLabel.setText("Status: Successfully created " + name);

				resourceNameInput.clear();
				capacityInput.clear();
			} catch (NumberFormatException ex) {
				statusLabel.setText("Status: Error! Capacity must be a number.");
				statusLabel.setStyle("-fx-text-fill: red;");
			}
		});

		Button logoutBtn = new Button("Log Out");
		logoutBtn.setOnAction(e -> app.showLogin());

		layout.getChildren().addAll(titleLabel, new Label("Manage Campus Resources:"), resourceNameInput, capacityInput,
				createBtn, statusLabel, logoutBtn);
		return layout;
	}
}