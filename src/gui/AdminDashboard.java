package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import main.AppSchedulerGUI;
import scheduling.Resource;
import users.User;

public class AdminDashboard {
	private AppSchedulerGUI app;

	public AdminDashboard(AppSchedulerGUI app) {
		this.app = app;
	}

	public VBox getContent() {
		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #FFFFFF;");

		Label titleLabel = new Label("Admin Dashboard - " + app.currentAdmin.getName());
		titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		// --- NEW: Double-click to verify Log Out ---
		Button logoutBtn = new Button("Log Out");
		logoutBtn.setStyle("-fx-cursor: hand;");
		logoutBtn.setOnAction(e -> {
			if (app.confirmAction("Confirm Log Out", "Are you sure you want to log out of the Admin dashboard?")) {
				app.showLogin();
			}
		});

		// --- NEW: Tabbed Navigation ---
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		Tab resourceTab = new Tab("Resource Management");
		resourceTab.setContent(createResourceTab());

		Tab requestTab = new Tab("Request Center");
		requestTab.setContent(createRequestTab());

		tabPane.getTabs().addAll(resourceTab, requestTab);
		VBox.setVgrow(tabPane, Priority.ALWAYS); // Makes the tabs fill the screen

		layout.getChildren().addAll(titleLabel, tabPane, logoutBtn);
		return layout;
	}

	// WINDOW 1: Resource Management (Your existing logic, moved into a Tab)
	private VBox createResourceTab() {
		VBox box = new VBox(15);
		box.setPadding(new Insets(20));

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

				app.activeResource = new Resource(name, capacity);
				statusLabel.setText("Status: Successfully created " + name);
				statusLabel.setStyle("-fx-text-fill: green;");

				resourceNameInput.clear();
				capacityInput.clear();
			} catch (NumberFormatException ex) {
				statusLabel.setText("Status: Error! Capacity must be a number.");
				statusLabel.setStyle("-fx-text-fill: red;");
			}
		});

		box.getChildren().addAll(new Label("Manage Campus Resources:"), resourceNameInput, capacityInput, createBtn,
				statusLabel);
		return box;
	}

	// WINDOW 2: Request Emails / Approval Center
	private VBox createRequestTab() {
		VBox box = new VBox(15);
		box.setPadding(new Insets(20));

		Label reqTitle = new Label("Pending Account Approvals:");
		reqTitle.setStyle("-fx-font-weight: bold;");

		// This automatically updates when someone uses the Sign Up screen!
		ListView<User> requestList = new ListView<>(app.pendingUsers);
		requestList.setPlaceholder(new Label("No pending requests at this time."));

		// Advanced JavaFX: Creating custom rows with Approve/Decline buttons inside the
		// list
		requestList.setCellFactory(param -> new ListCell<User>() {
			@Override
			protected void updateItem(User user, boolean empty) {
				super.updateItem(user, empty);
				if (empty || user == null) {
					setText(null);
					setGraphic(null);
				} else {
					HBox row = new HBox(15);
					row.setAlignment(Pos.CENTER_LEFT);

					Label userInfo = new Label(user.getName() + " (" + user.getUserId() + ") - Requesting "
							+ user.getClass().getSimpleName() + " Account");
					Region spacer = new Region();
					HBox.setHgrow(spacer, Priority.ALWAYS); // Pushes buttons to the right

					Button btnApprove = new Button("Approve");
					btnApprove.setStyle("-fx-background-color: #34a853; -fx-text-fill: white; -fx-cursor: hand;");
					btnApprove.setOnAction(e -> {
						user.setApproved(true); // Grant access
						app.pendingUsers.remove(user); // Remove from the pending queue
					});

					Button btnDecline = new Button("Decline");
					btnDecline.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white; -fx-cursor: hand;");
					btnDecline.setOnAction(e -> {
						// Remove them completely so they have to sign up again
						app.pendingUsers.remove(user);
					});

					row.getChildren().addAll(userInfo, spacer, btnApprove, btnDecline);
					setGraphic(row);
				}
			}
		});

		box.getChildren().addAll(reqTitle, requestList);
		return box;
	}
}