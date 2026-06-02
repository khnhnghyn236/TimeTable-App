package gui;

import javafx.animation.FadeTransition;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import main.AppSchedulerGUI;
import main.UserPersistence;
import users.*;

public class LoginScreen {
    private static final String BLUE_DARK = "#0C447C";
    private static final String BLUE_MID = "#185FA5";
    private static final String BLUE_LIGHT = "#378ADD";
    private static final String BLUE_MUTED = "#85B7EB";
    private static final String WHITE = "#FFFFFF";
    private static final String BG_PAGE = "#F5F7FA";
    private static final String BG_FIELD = "#EEF2F7";
    private static final String TEXT_DARK = "#1A1A2E";
    private static final String TEXT_MUTED = "#5F6368";
    private static final String ERROR_RED = "#D93025";
    private static final String WARN_AMBER = "#B06000";
    private static final String WARN_BG = "#FEF7E0";

    private final AppSchedulerGUI app;

    public LoginScreen(AppSchedulerGUI app) {
        this.app = app;
    }

    public HBox getContent() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: " + BG_PAGE + ";");
        root.getChildren().addAll(buildSidebar(), buildForm());
        return root;
    }

    private VBox buildSidebar() {
        // STEP 1: Set up the visual branding sidebar
        VBox side = new VBox(24);
        side.setPrefWidth(220);
        side.setMinWidth(220);
        side.setPadding(new Insets(40, 28, 32, 28));
        side.setStyle("-fx-background-color: " + BLUE_DARK + ";");

        Label appName = new Label("TimeTable");
        appName.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: "
                + WHITE + ";");

        Label tagline = new Label("APPOINTMENT PLATFORM");
        tagline.setStyle("-fx-font-size: 10px; -fx-text-fill: " + BLUE_MUTED + ";");

        VBox brand = new VBox(4, appName, tagline);
        VBox feats = new VBox(20,
                featureItem("Smart Scheduling", "Book rooms & slots instantly"),
                featureItem("Secure Access", "Role-based admin approval"),
                featureItem("Notifications", "Real-time booking updates"));
        VBox.setVgrow(feats, Priority.ALWAYS);

        Label footer = new Label("VinUniversity © 2025");
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: " + BLUE_LIGHT + ";");

        side.getChildren().addAll(brand, new Separator(), feats, footer);
        return side;
    }

    // Feature item style: title + description
    private VBox featureItem(String title, String desc) {
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + WHITE + ";");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + BLUE_MUTED + ";");
        descLbl.setWrapText(true);
        return new VBox(2, titleLbl, descLbl);
    }

    private VBox buildForm() {
        // STEP 1: Set up the main login form container
        VBox form = new VBox(0);
        form.setPadding(new Insets(48, 44, 48, 44));
        form.setAlignment(Pos.CENTER_LEFT);
        form.setStyle("-fx-background-color: " + WHITE + ";");
        HBox.setHgrow(form, Priority.ALWAYS);

        Label heading = new Label("Welcome back");
        heading.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: "
                + TEXT_DARK + ";");
        Label subheading = new Label("Sign in to your TimeTable account");
        subheading.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MUTED + ";");
        VBox headingBox = new VBox(4, heading, subheading);
        headingBox.setPadding(new Insets(0, 0, 24, 0));

        // STEP 2: Initialize User ID input field and load saved ID if available
        Label idLbl = fieldLabel("Student / Staff ID");
        TextField idField = styledTextField("e.g. V202502059");
        Label idErr = errorLabel();

        String remembered = UserPersistence.loadRememberedId();
        if (remembered != null)
            idField.setText(remembered);

        // STEP 3: Setup Password field with visibility toggle
        Label passLbl = fieldLabel("Password");
        PasswordField passField = styledPasswordField("Enter your password");
        TextField passVisible = styledTextField("");
        passVisible.setVisible(false);
        passVisible.setManaged(false);
        Label passErr = errorLabel();

        Button eyeBtn = new Button("Show");
        eyeBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand; -fx-font-size: 12px;");
        final boolean[] showing = { false };
        eyeBtn.setOnAction(ev -> {
            showing[0] = !showing[0];
            if (showing[0]) {
                passVisible.setText(passField.getText());
                passVisible.setVisible(true);
                passVisible.setManaged(true);
                passField.setVisible(false);
                passField.setManaged(false);
                eyeBtn.setText("Hide");
            } else {
                passField.setText(passVisible.getText());
                passField.setVisible(true);
                passField.setManaged(true);
                passVisible.setVisible(false);
                passVisible.setManaged(false);
                eyeBtn.setText("Show");
            }
        });

        HBox passRow = new HBox(0, passField, passVisible, eyeBtn);
        passRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(passField, Priority.ALWAYS);
        HBox.setHgrow(passVisible, Priority.ALWAYS);
        passRow.setStyle("-fx-border-color: #C8D4E8; -fx-border-radius: 8; -fx-background-color: " + BG_FIELD
                + "; -fx-background-radius: 8;");

        CheckBox rememberBox = new CheckBox("Remember my ID");
        rememberBox.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MUTED + ";");
        rememberBox.setSelected(remembered != null);
        rememberBox.setPadding(new Insets(8, 0, 16, 0));

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(320);

        // STEP 4: Define login logic and role-based routing
        Button loginBtn = primaryButton("Sign In");
        loginBtn.setOnAction(ev -> {
            idErr.setText("");
            passErr.setText("");
            statusLabel.setText("");
            statusLabel.setStyle("");

            String id = idField.getText().trim();
            String pass = showing[0] ? passVisible.getText() : passField.getText();

            if (id.isEmpty()) {
                idErr.setText("ID cannot be empty.");
                shake(idField);
                return;
            }
            if (pass.isEmpty()) {
                passErr.setText("Password cannot be empty.");
                shake(passRow);
                return;
            }
            if (!app.userDatabase.containsKey(id)) {
                idErr.setText("No account found with this ID.");
                shake(idField);
                return;
            }

            User user = app.userDatabase.get(id);
            if (!user.verifyPassword(pass)) {
                passErr.setText("Incorrect password. Please try again.");
                shake(passRow);
                return;
            }
            if (!user.isApproved()) {
                showStatus(statusLabel, "Your account is pending admin approval.", WARN_AMBER, WARN_BG);
                return;
            }

            if (rememberBox.isSelected())
                UserPersistence.saveRememberedId(id);
            else
                UserPersistence.clearRememberedId();

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
                showStatus(statusLabel, "Unknown account role. Contact admin.", ERROR_RED, "#FDE8E8");
            }
        });

        passField.setOnKeyPressed(ev -> {
            if (ev.getCode() == javafx.scene.input.KeyCode.ENTER) {
                loginBtn.fire();
            }
        });

        // STEP 5: Add sign-up redirection link for new users
        Separator sep = new Separator();
        sep.setPadding(new Insets(16, 0, 12, 0));

        Label signupPrompt = new Label("Not a member?  ");
        signupPrompt.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MUTED + ";");
        Label signupLink = new Label("Create an account");
        signupLink.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: " + BLUE_MID + "; -fx-cursor: hand; -fx-underline: true;");
        signupLink.setOnMouseClicked(ev -> app.showSignUp());
        HBox signupRow = new HBox(0, signupPrompt, signupLink);
        signupRow.setAlignment(Pos.CENTER_LEFT);

        VBox idBlock = new VBox(4, idLbl, idField, idErr);
        idBlock.setPadding(new Insets(0, 0, 14, 0));
        VBox passBlock = new VBox(4, passLbl, passRow, passErr);
        passBlock.setPadding(new Insets(0, 0, 4, 0));
        VBox btnBlock = new VBox(10, loginBtn, statusLabel);
        btnBlock.setPadding(new Insets(4, 0, 0, 0));

        form.getChildren().addAll(headingBox, idBlock, passBlock, rememberBox, btnBlock, sep, signupRow);
        return form;
    }

    // Field label style
    private Label fieldLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_MUTED + ";");
        return l;
    }

    // Text field style
    private TextField styledTextField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: " + BG_FIELD
                + "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #C8D4E8; -fx-pref-height: 40px; -fx-font-size: 14px; -fx-padding: 0 12 0 12;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    // Password field style
    private PasswordField styledPasswordField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle(
                "-fx-background-color: transparent; -fx-pref-height: 40px; -fx-font-size: 14px; -fx-padding: 0 12 0 12;");
        f.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(f, Priority.ALWAYS);
        return f;
    }

    // Error message style
    private Label errorLabel() {
        Label l = new Label();
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + ERROR_RED + ";");
        l.setWrapText(true);
        return l;
    }

    // Primary button style
    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(44);
        b.setStyle("-fx-background-color: " + BLUE_MID
                + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        return b;
    }

    // Show status message
    private void showStatus(Label lbl, String msg, String textColor, String bgColor) {
        lbl.setText(msg);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + textColor + "; -fx-background-color: " + bgColor
                + "; -fx-background-radius: 6; -fx-padding: 8 12 8 12;");
        FadeTransition ft = new FadeTransition(Duration.millis(300), lbl);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // Shake animation --> used when error occurs
    private void shake(javafx.scene.Node node) {
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(Duration.millis(60), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }
}
