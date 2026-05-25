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

    public AdminDashboard(AppSchedulerGUI app) { this.app = app; }

    public VBox getContent() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #FFFFFF;");

        Label titleLabel = new Label("Admin Dashboard - " + app.currentAdmin.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setStyle("-fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            if (app.confirmAction("Confirm Log Out", "Are you sure you want to log out of the Admin dashboard?")) {
                app.showLogin();
            }
        });

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab resourceTab = new Tab("Room Management");
        resourceTab.setContent(createResourceTab());

        Tab requestTab = new Tab("Request Center");
        requestTab.setContent(createRequestTab());

        tabPane.getTabs().addAll(resourceTab, requestTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        layout.getChildren().addAll(titleLabel, tabPane, logoutBtn);
        return layout;
    }

    private VBox createResourceTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label header = new Label("Create and manage bookable campus rooms:");
        header.setStyle("-fx-font-weight: bold;");

        TextField resourceNameInput = new TextField();
        resourceNameInput.setPromptText("Room name, e.g., Library Study Room B");

        TextField capacityInput = new TextField();
        capacityInput.setPromptText("Capacity, e.g., 4");

        Button createBtn = new Button("Create Room");
        createBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-cursor: hand;");

        Label statusLabel = new Label("Status: Ready");
        statusLabel.setStyle("-fx-text-fill: green;");

        ListView<Resource> resourceList = new ListView<>(app.resourceCatalog);
        resourceList.setPlaceholder(new Label("No room has been created yet."));
        resourceList.setCellFactory(param -> new ListCell<Resource>() {
            @Override
            protected void updateItem(Resource resource, boolean empty) {
                super.updateItem(resource, empty);
                if (empty || resource == null) {
                    setText(null);
                } else {
                    setText(resource.getName() + "   •   Capacity: " + resource.getCapacity());
                }
            }
        });
        VBox.setVgrow(resourceList, Priority.ALWAYS);

        createBtn.setOnAction(e -> {
            try {
                String name = resourceNameInput.getText().trim();
                int capacity = Integer.parseInt(capacityInput.getText().trim());

                if (name.isEmpty()) {
                    showStatus(statusLabel, "Status: Error! Room name cannot be empty.", true);
                    return;
                }
                if (name.contains("|") || name.contains(",")) {
                    showStatus(statusLabel, "Status: Error! Please avoid | and comma in room names.", true);
                    return;
                }
                if (capacity <= 0) {
                    showStatus(statusLabel, "Status: Error! Capacity must be greater than 0.", true);
                    return;
                }
                if (app.resourceNameExists(name)) {
                    showStatus(statusLabel, "Status: Error! This room already exists.", true);
                    return;
                }

                Resource resource = new Resource(name, capacity);
                app.addResource(resource);
                showStatus(statusLabel, "Status: Successfully created " + name, false);
                resourceNameInput.clear();
                capacityInput.clear();
            } catch (NumberFormatException ex) {
                showStatus(statusLabel, "Status: Error! Capacity must be a number.", true);
            }
        });

        HBox form = new HBox(10, resourceNameInput, capacityInput, createBtn);
        HBox.setHgrow(resourceNameInput, Priority.ALWAYS);
        capacityInput.setMaxWidth(120);

        box.getChildren().addAll(header, form, statusLabel, new Label("Available Rooms:"), resourceList);
        return box;
    }

    private void showStatus(Label label, String message, boolean error) {
        label.setText(message);
        label.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }

    private VBox createRequestTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label reqTitle = new Label("Pending Account Approvals:");
        reqTitle.setStyle("-fx-font-weight: bold;");

        ListView<User> requestList = new ListView<>(app.pendingUsers);
        requestList.setPlaceholder(new Label("No pending requests at this time."));

        requestList.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);

                Label userInfo = new Label(user.getName() + " (" + user.getUserId() + ") - " +
                        user.getEmail() + " - Requesting " + user.getClass().getSimpleName() + " Account");
                userInfo.setWrapText(true);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnApprove = new Button("Approve");
                btnApprove.setStyle("-fx-background-color: #34a853; -fx-text-fill: white; -fx-cursor: hand;");
                btnApprove.setOnAction(e -> app.approveUser(user));

                Button btnDecline = new Button("Decline");
                btnDecline.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white; -fx-cursor: hand;");
                btnDecline.setOnAction(e -> {
                    if (app.confirmAction("Confirm Decline", "Decline this account request permanently?")) {
                        app.declineUser(user);
                    }
                });

                row.getChildren().addAll(userInfo, spacer, btnApprove, btnDecline);
                setGraphic(row);
            }
        });

        box.getChildren().addAll(reqTitle, requestList);
        return box;
    }
}
