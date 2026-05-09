package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.AppSchedulerGUI;
import users.AcademicStaff;
import users.Student;
import users.User;

public class SignUpScreen {
	private AppSchedulerGUI app;

	public SignUpScreen(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);
		layout.setStyle("-fx-background-color: #FFFFFF;");

		Label title = new Label("Create an Account");
		title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

		// Form Fields
		TextField nameInput = new TextField();
		nameInput.setPromptText("Full Name");

		TextField idInput = new TextField();
		idInput.setPromptText("Student/Faculty ID");

		TextField emailInput = new TextField();
		emailInput.setPromptText("Email Address");

		PasswordField passInput = new PasswordField();
		passInput.setPromptText("Password");

		PasswordField passConfirm = new PasswordField();
		passConfirm.setPromptText("Check Password Again");

		ComboBox<String> roleBox = new ComboBox<>();
		roleBox.getItems().addAll("Student", "Academic Staff");
		roleBox.setPromptText("Select Role");

		Label statusLabel = new Label();

		Button btnSubmit = new Button("Sign Up");
		btnSubmit.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-cursor: hand;");

		Button btnBack = new Button("Back to Login");
		btnBack.setOnAction(e -> app.showLogin());

		btnSubmit.setOnAction(e -> {
			String id = idInput.getText();
			String name = nameInput.getText();
			String email = emailInput.getText();
			String pass = passInput.getText();
			String confirm = passConfirm.getText();
			String role = roleBox.getValue();

			// Validation
			if (id.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty() || role == null) {
				statusLabel.setText("Please fill in all fields.");
				statusLabel.setStyle("-fx-text-fill: red;");
				return;
			}
			if (!pass.equals(confirm)) {
				statusLabel.setText("Passwords do not match.");
				statusLabel.setStyle("-fx-text-fill: red;");
				return;
			}
			if (app.userDatabase.containsKey(id)) {
				statusLabel.setText("Account ID already exists.");
				statusLabel.setStyle("-fx-text-fill: red;");
				return;
			}

			// Create User and add to Pending List
			User newUser;
			if (role.equals("Student")) {
				newUser = new Student(id, name, email, pass);
			} else {
				newUser = new AcademicStaff(id, name, email, pass);
			}

			app.userDatabase.put(id, newUser);
			app.pendingUsers.add(newUser);

			// Show the "Thank You" success state within the same window
			layout.getChildren().clear();
			Label successTitle = new Label("Thank you for signing up!");
			successTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: green;");
			Label successMsg = new Label("Please wait for approval from an Administrator.");
			layout.getChildren().addAll(successTitle, successMsg, btnBack);
		});

		layout.getChildren().addAll(title, roleBox, nameInput, idInput, emailInput, passInput, passConfirm, btnSubmit,
				statusLabel, new Separator(), btnBack);
		return layout;
	}
}