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

    public SignUpScreen(AppSchedulerGUI app) { this.app = app; }

    public VBox getContent() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FFFFFF;");

        Label title = new Label("Create an Account");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Full Name");
        nameInput.setMaxWidth(280);

        TextField idInput = new TextField();
        idInput.setPromptText("Student/Faculty ID");
        idInput.setMaxWidth(280);

        TextField emailInput = new TextField();
        emailInput.setPromptText("Email Address");
        emailInput.setMaxWidth(280);

        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Password");
        passInput.setMaxWidth(280);

        PasswordField passConfirm = new PasswordField();
        passConfirm.setPromptText("Confirm Password");
        passConfirm.setMaxWidth(280);

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Student", "Academic Staff");
        roleBox.setPromptText("Select Role");
        roleBox.setMaxWidth(280);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(280);

        Button btnSubmit = new Button("Sign Up");
        btnSubmit.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-cursor: hand;");

        Button btnBack = new Button("Back to Login");
        btnBack.setOnAction(e -> app.showLogin());

        btnSubmit.setOnAction(e -> {
            String id = idInput.getText().trim();
            String name = nameInput.getText().trim();
            String email = emailInput.getText().trim();
            String pass = passInput.getText();
            String confirm = passConfirm.getText();
            String role = roleBox.getValue();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty() || role == null) {
                showError(statusLabel, "Please fill in all fields."); return;
            }
            if (id.contains("|") || name.contains("|") || email.contains("|") || pass.contains("|")) {
                showError(statusLabel, "Please do not use the | character."); return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                showError(statusLabel, "Please enter a valid email address."); return;
            }
            if (!pass.equals(confirm)) {
                showError(statusLabel, "Passwords do not match."); return;
            }
            if (pass.length() < 6) {
                showError(statusLabel, "Password must be at least 6 characters."); return;
            }
            if (app.userDatabase.containsKey(id)) {
                showError(statusLabel, "Account ID already exists."); return;
            }
            if (app.emailExists(email)) {
                showError(statusLabel, "Email address already exists."); return;
            }

            User newUser = role.equals("Student")
                    ? new Student(id, name, email, pass)
                    : new AcademicStaff(id, name, email, pass);

            app.registerNewUser(newUser);

            layout.getChildren().clear();
            Label successTitle = new Label("Thank you for signing up!");
            successTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: green;");
            Label successMsg = new Label("Your account is pending approval from an Administrator.");
            successMsg.setWrapText(true);
            layout.getChildren().addAll(successTitle, successMsg, btnBack);
        });

        layout.getChildren().addAll(title, roleBox, nameInput, idInput, emailInput, passInput, passConfirm,
                btnSubmit, statusLabel, new Separator(), btnBack);
        return layout;
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: red;");
    }
}
