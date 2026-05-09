package gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import main.AppSchedulerGUI;

public class LoginScreen {
	private AppSchedulerGUI app;

	public LoginScreen(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(15);
		layout.setAlignment(Pos.CENTER);
		layout.setStyle("-fx-background-color: #FFFFFF;");

		Label welcomeLabel = new Label("Welcome to TimeTable");
		welcomeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333333;");

		// Role Selection Buttons 
		Button btnStudent = new Button("Login as Student");
		Button btnStaff = new Button("Login as Academic Staff");
		Button btnAdmin = new Button("Login as Administrator");

		// Styling buttons to match the light theme
		String btnStyle = "-fx-background-color: #f1f3f4; -fx-text-fill: #3c4043; -fx-border-color: #dadce0; -fx-pref-width: 200px; -fx-cursor: hand;";
		btnStudent.setStyle(btnStyle);
		btnStaff.setStyle(btnStyle);
		btnAdmin.setStyle(btnStyle);

		// Navigation Actions
		btnStudent.setOnAction(e -> app.showStudent());
		btnStaff.setOnAction(e -> app.showStaff());
		btnAdmin.setOnAction(e -> app.showAdmin());

		layout.getChildren().addAll(welcomeLabel, new Label("Please select your role:"), btnStudent, btnStaff,
				btnAdmin);
		return layout;
	}
}