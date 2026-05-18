package gui;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.AppSchedulerGUI;
import users.*;

public class LoginScreen {
	private AppSchedulerGUI app;

	public LoginScreen(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(15);
		layout.setAlignment(Pos.CENTER);
		layout.setStyle("-fx-background-color: #FFFFFF;");

		Label welcomeLabel = new Label("TimeTable Login");
		welcomeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333333;");

		TextField idInput = new TextField();
		idInput.setPromptText("Enter ID (e.g., V202502059)");
		idInput.setMaxWidth(200);

		PasswordField passInput = new PasswordField();
		passInput.setPromptText("Password");
		passInput.setMaxWidth(200);

		Label warningLabel = new Label();

		Button btnLogin = new Button("Login");
		btnLogin.setStyle(
				"-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-pref-width: 200px; -fx-cursor: hand;");

		btnLogin.setOnAction(e -> {
			String id = idInput.getText().trim();
			String pass = passInput.getText();

			if (id.isEmpty() || pass.isEmpty()) {
				warningLabel.setText("Please enter ID and password.");
				warningLabel.setStyle("-fx-text-fill: red;");
				return;
			}

			if (!app.userDatabase.containsKey(id)) {
				warningLabel.setText("Account does not exist.");
				warningLabel.setStyle("-fx-text-fill: red;");
				return;
			}

			User user = app.userDatabase.get(id);

			if (!user.getPassword().equals(pass)) {
				warningLabel.setText("Incorrect password.");
				warningLabel.setStyle("-fx-text-fill: red;");
				return;
			}

			if (!user.isApproved()) {
				warningLabel.setText("Admin has not approved this account yet.");
				warningLabel.setStyle("-fx-text-fill: #d4a017;");
				return;
			}

			if (user instanceof Administrator) {
				app.currentAdmin = (Administrator) user;
				app.showAdmin();
			} else if (user instanceof Student) {
				app.currentStudent = (Student) user;
				app.showStudent();
			} else if (user instanceof AcademicStaff) {
				app.currentStaff = (AcademicStaff) user;
				app.showStaff();
			} else {
				warningLabel.setText("Unknown account role.");
				warningLabel.setStyle("-fx-text-fill: red;");
			}
		});

		Button btnAdmin = new Button("Use Demo Admin Account");
		btnAdmin.setStyle("-fx-background-color: transparent; -fx-text-fill: #5f6368; -fx-cursor: hand;");
		btnAdmin.setOnAction(e -> {
			idInput.setText("A001");
			passInput.setText("admin123");
			warningLabel.setText("Demo admin filled. Click Login.");
			warningLabel.setStyle("-fx-text-fill: #5f6368;");
		});

		Label signupText = new Label("Not a member? Create an account here!");
		signupText.setStyle("-fx-text-fill: #1a73e8; -fx-cursor: hand; -fx-underline: true;");
		signupText.setOnMouseClicked(e -> app.showSignUp());

		layout.getChildren().addAll(welcomeLabel, idInput, passInput, btnLogin, warningLabel, btnAdmin, new Separator(),
				signupText);
		return layout;
	}
}
