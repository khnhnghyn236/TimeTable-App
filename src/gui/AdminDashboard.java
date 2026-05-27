package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import main.AppSchedulerGUI;
import scheduling.Resource;
import scheduling.TimeSlot;
import users.User;
import java.util.stream.Collectors;
import java.util.List;

public class AdminDashboard {
    private AppSchedulerGUI app;
    private TabPane tabPane;
    private ListView<Resource> resourceList;
    private ListView<TimeSlot> scheduleList;

    public AdminDashboard(AppSchedulerGUI app) {
        this.app = app;
    }

    public VBox getContent() {
        VBox layout = new VBox(0);
        layout.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        // --- Header bar matching LoginScreen sidebar style ---
        HBox header = new HBox();
        header.setPadding(new Insets(16, 28, 16, 28));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0C447C;");

        Label appName = new Label("TimeTable");
        appName.setStyle(
                "-fx-font-family: 'Georgia'; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sep = new Label("  |  ");
        sep.setStyle("-fx-text-fill: #85B7EB; -fx-font-size: 18px;");

        Label titleLabel = new Label("Admin Dashboard - " + app.currentAdmin.getName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #D1E8FF;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #85B7EB; -fx-border-radius: 6; " +
                        "-fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 14;");
        logoutBtn.setOnAction(e -> {
            if (app.confirmAction("Confirm Log Out", "Are you sure you want to log out of the Admin dashboard?")) {
                app.showLogin();
            }
        });

        header.getChildren().addAll(appName, sep, titleLabel, spacer, logoutBtn);

        // --- Tabs ---
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        Tab resourceTab = new Tab("Room Management");
        resourceTab.setContent(createResourceTab());

        Tab scheduleTab = new Tab("Schedule Approvals");
        scheduleTab.setContent(createScheduleTab());
        scheduleTab.setOnSelectionChanged(e -> {
            if (scheduleTab.isSelected())
                updateScheduleList();
        });

        Tab requestTab = new Tab("Account Requests");
        requestTab.setContent(createRequestTab());

        Tab notifTab = new Tab("Notifications");
        notifTab.setContent(createNotifyTab());

        tabPane.getTabs().addAll(resourceTab, scheduleTab, requestTab, notifTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        layout.getChildren().addAll(header, tabPane);
        return layout;
    }

    private VBox createResourceTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        Label header = new Label("Create and manage bookable campus rooms:");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A1A2E;");

        TextField resourceNameInput = new TextField();
        resourceNameInput.setPromptText("Room name, e.g., Library Study Room B");
        resourceNameInput.setStyle("-fx-background-color: " + AppStyles.WHITE
                + "; -fx-border-color: #C8D4E8; -fx-border-radius: 6; -fx-pref-height: 36px;");

        TextField capacityInput = new TextField();
        capacityInput.setPromptText("Capacity");
        capacityInput.setStyle("-fx-background-color: " + AppStyles.WHITE
                + "; -fx-border-color: #C8D4E8; -fx-border-radius: 6; -fx-pref-height: 36px;");

        Button createBtn = AppStyles.primaryButton("Create Room");
        createBtn.setPrefHeight(36);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        resourceList = new ListView<>(app.resourceCatalog);
        resourceList.setPlaceholder(new Label("No room has been created yet."));
        resourceList.setStyle("-fx-background-color: " + AppStyles.WHITE + "; -fx-border-color: #E2E8F0;");
        resourceList.setCellFactory(param -> new ListCell<Resource>() {
            @Override
            protected void updateItem(Resource resource, boolean empty) {
                super.updateItem(resource, empty);
                if (empty || resource == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(6, 10, 6, 10));

                    Label nameLbl = new Label(resource.getName());
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                    Label capLbl = new Label("Capacity: " + resource.getCapacity());
                    capLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");
                    VBox info = new VBox(2, nameLbl, capLbl);

                    Region sp = new Region();
                    HBox.setHgrow(sp, Priority.ALWAYS);

                    Button editBtn = AppStyles.ghostButton("Edit");
                    editBtn.setOnAction(e -> {
                        TextInputDialog dialog = new TextInputDialog(resource.getName() + "," + resource.getCapacity());
                        dialog.setTitle("Edit Room");
                        dialog.setHeaderText("Edit room details");
                        dialog.setContentText("Format: Name,Capacity (e.g. Room A,40):");
                        dialog.showAndWait().ifPresent(result -> {
                            try {
                                String[] parts = result.split(",");
                                if (parts.length == 2) {
                                    String newName = parts[0].trim();
                                    int newCap = Integer.parseInt(parts[1].trim());
                                    if (!newName.isEmpty() && newCap > 0) {
                                        resource.setName(newName);
                                        resource.setCapacity(newCap);
                                        main.DataManager.saveResources(app.resourceCatalog);
                                        resourceList.refresh();
                                        AppStyles.showStatus(statusLabel, "Updated " + newName,
                                                AppStyles.STATUS_APPROVED, "#D1FAE5");
                                    }
                                }
                            } catch (Exception ex) {
                                AppStyles.showStatus(statusLabel, "Invalid format.", AppStyles.ERROR_RED,
                                        AppStyles.WARN_BG);
                            }
                        });
                    });

                    Button delBtn = AppStyles.dangerButton("Delete");
                    delBtn.setOnAction(e -> {
                        if (app.confirmAction("Delete Room",
                                "Delete " + resource.getName() + "? This won't delete existing schedules using it.")) {
                            app.resourceCatalog.remove(resource);
                            main.DataManager.saveResources(app.resourceCatalog);
                            AppStyles.showStatus(statusLabel, "Deleted " + resource.getName(), AppStyles.TEXT_MUTED,
                                    AppStyles.WARN_BG);
                        }
                    });

                    row.getChildren().addAll(info, sp, editBtn, delBtn);
                    setGraphic(row);
                }
            }
        });
        VBox.setVgrow(resourceList, Priority.ALWAYS);

        createBtn.setOnAction(e -> {
            try {
                String name = resourceNameInput.getText().trim();
                int capacity = Integer.parseInt(capacityInput.getText().trim());

                if (name.isEmpty()) {
                    AppStyles.showStatus(statusLabel, "Room name cannot be empty.", AppStyles.ERROR_RED,
                            AppStyles.WARN_BG);
                    return;
                }
                if (name.contains("|") || name.contains(",")) {
                    AppStyles.showStatus(statusLabel, "Please avoid | and comma in names.", AppStyles.ERROR_RED,
                            AppStyles.WARN_BG);
                    return;
                }
                if (capacity <= 0) {
                    AppStyles.showStatus(statusLabel, "Capacity must be > 0.", AppStyles.ERROR_RED, AppStyles.WARN_BG);
                    return;
                }
                if (app.resourceNameExists(name)) {
                    AppStyles.showStatus(statusLabel, "This room already exists.", AppStyles.ERROR_RED,
                            AppStyles.WARN_BG);
                    return;
                }

                Resource resource = new Resource(name, capacity);
                app.addResource(resource); // This broadcasts notification to ALL
                AppStyles.showStatus(statusLabel, "Created: " + name, AppStyles.STATUS_APPROVED, "#D1FAE5");
                resourceNameInput.clear();
                capacityInput.clear();
            } catch (NumberFormatException ex) {
                AppStyles.showStatus(statusLabel, "Capacity must be a number.", AppStyles.ERROR_RED, AppStyles.WARN_BG);
            }
        });

        HBox form = new HBox(10, resourceNameInput, capacityInput, createBtn);
        form.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(resourceNameInput, Priority.ALWAYS);
        capacityInput.setMaxWidth(100);

        Label roomsHeader = new Label("Available Rooms:");
        roomsHeader
                .setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");

        box.getChildren().addAll(header, form, statusLabel, roomsHeader, resourceList);
        return box;
    }

    private VBox createScheduleTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        Label reqTitle = new Label("Manage Faculty Timeslot Requests:");
        reqTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A1A2E;");

        scheduleList = new ListView<>();
        scheduleList.setPlaceholder(new Label("No pending timeslot requests."));
        scheduleList.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        scheduleList.setCellFactory(param -> new ListCell<TimeSlot>() {
            @Override
            protected void updateItem(TimeSlot slot, boolean empty) {
                super.updateItem(slot, empty);
                if (empty || slot == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 12, 8, 12));

                // Status indicator bar
                String statusColor;
                if ("APPROVED".equals(slot.getStatus()))
                    statusColor = AppStyles.STATUS_APPROVED;
                else if ("DECLINED".equals(slot.getStatus()))
                    statusColor = AppStyles.STATUS_DECLINED;
                else
                    statusColor = AppStyles.STATUS_PENDING;

                Label indicator = new Label();
                indicator.setMinWidth(5);
                indicator.setPrefWidth(5);
                indicator.setPrefHeight(50);
                indicator.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 3;");

                VBox infoBox = new VBox(4);
                Label titleLbl = new Label(slot.getTitle() + " @ " + slot.getResource().getName());
                titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A1A2E;");
                Label timeLbl = new Label(slot.getLocalDate() + "  " + slot.getStartTime() + " – " + slot.getEndTime());
                timeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");
                Label metaLbl = new Label("By: " + slot.getCreatorName() + "  |  Status: " + slot.getStatus()
                        + "  |  Slots: " + slot.getSlotCapacity());
                metaLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
                infoBox.getChildren().addAll(titleLbl, timeLbl, metaLbl);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button approveBtn = AppStyles.successButton("Approve");
                approveBtn.setOnAction(e -> {
                    slot.setStatus("APPROVED");
                    app.addNotification(slot.getCreatorId(),
                            "✅ Your timeslot \"" + slot.getTitle() + "\" on " + slot.getLocalDate()
                                    + " (" + slot.getStartTime() + " – " + slot.getEndTime()
                                    + ") was approved by Admin.");
                    main.DataManager.saveState(app.systemTimeSlots);
                    updateScheduleList();
                });

                Button declineBtn = AppStyles.dangerButton("Decline");
                declineBtn.setOnAction(e -> {
                    slot.setStatus("DECLINED");
                    app.addNotification(slot.getCreatorId(),
                            "❌ Your timeslot \"" + slot.getTitle() + "\" on " + slot.getLocalDate()
                                    + " (" + slot.getStartTime() + " – " + slot.getEndTime()
                                    + ") was declined by Admin.");
                    main.DataManager.saveState(app.systemTimeSlots);
                    updateScheduleList();
                });

                Button deleteBtn = AppStyles.ghostButton("Delete");
                deleteBtn.setStyle("-fx-border-color: " + AppStyles.ERROR_RED + "; -fx-text-fill: "
                        + AppStyles.ERROR_RED + "; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    if (app.confirmAction("Delete Schedule", "Permanently delete this schedule?")) {
                        app.systemTimeSlots.remove(slot);
                        main.DataManager.saveState(app.systemTimeSlots);
                        updateScheduleList();
                    }
                });

                if ("PENDING".equals(slot.getStatus())) {
                    row.getChildren().addAll(indicator, infoBox, spacer, approveBtn, declineBtn);
                } else {
                    row.getChildren().addAll(indicator, infoBox, spacer, deleteBtn);
                }
                setGraphic(row);
            }
        });

        VBox.setVgrow(scheduleList, Priority.ALWAYS);
        box.getChildren().addAll(reqTitle, scheduleList);
        return box;
    }

    private void updateScheduleList() {
        List<TimeSlot> sorted = app.systemTimeSlots.stream()
                .sorted((a, b) -> {
                    if (a.getStatus().equals("PENDING") && !b.getStatus().equals("PENDING"))
                        return -1;
                    if (!a.getStatus().equals("PENDING") && b.getStatus().equals("PENDING"))
                        return 1;
                    return a.getTimeRange().compareTo(b.getTimeRange());
                })
                .collect(Collectors.toList());
        scheduleList.setItems(FXCollections.observableArrayList(sorted));
    }

    private VBox createRequestTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        Label reqTitle = new Label("Pending Account Approvals:");
        reqTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A1A2E;");

        ListView<User> requestList = new ListView<>(app.pendingUsers);
        requestList.setPlaceholder(new Label("No pending requests at this time."));
        requestList.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

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
                row.setPadding(new Insets(8, 12, 8, 12));

                VBox infoBox = new VBox(4);
                Label nameLbl = new Label(user.getName() + " (" + user.getUserId() + ")");
                nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A1A2E;");
                Label detailLbl = new Label(
                        user.getEmail() + "  •  Requesting: " + user.getClass().getSimpleName() + " Account");
                detailLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");
                infoBox.getChildren().addAll(nameLbl, detailLbl);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnApprove = AppStyles.successButton("Approve");
                btnApprove.setOnAction(e -> app.approveUser(user));

                Button btnDecline = AppStyles.dangerButton("Decline");
                btnDecline.setOnAction(e -> {
                    if (app.confirmAction("Confirm Decline", "Decline this account request permanently?")) {
                        app.declineUser(user);
                    }
                });

                row.getChildren().addAll(infoBox, spacer, btnApprove, btnDecline);
                setGraphic(row);
            }
        });

        VBox.setVgrow(requestList, Priority.ALWAYS);
        box.getChildren().addAll(reqTitle, requestList);
        return box;
    }

    private VBox createNotifyTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        ObservableList<VBox> items = FXCollections.observableArrayList();

        java.util.List<scheduling.Notification> myNotifs = new java.util.ArrayList<>();
        for (scheduling.Notification n : app.systemNotifications) {
            if (n.getUserId().equals(app.currentAdmin.getUserId())) {
                myNotifs.add(n);
            }
        }
        java.util.Collections.reverse(myNotifs);

        for (scheduling.Notification n : myNotifs) {
            items.add(buildNotifCard(n));
        }

        ListView<VBox> list = new ListView<>(items);
        list.setPlaceholder(new Label("No notifications yet."));
        list.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");
        VBox.setVgrow(list, Priority.ALWAYS);

        Label titleLbl = new Label("My Notifications");
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label countLbl = new Label(myNotifs.size() + " message(s)");
        countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");

        HBox topRow = new HBox(10, titleLbl, new Region(), countLbl);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button clearBtn = AppStyles.dangerButton("Clear All Notifications");
        clearBtn.setPrefHeight(36);
        clearBtn.setOnAction(e -> {
            if (app.confirmAction("Clear Notifications", "Remove all your notifications?")) {
                app.clearNotifications(app.currentAdmin.getUserId());
                items.clear();
            }
        });

        box.getChildren().addAll(topRow, list, clearBtn);
        return box;
    }

    private VBox buildNotifCard(scheduling.Notification n) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle("-fx-background-color: " + AppStyles.WHITE
                + "; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label timeLbl = new Label(n.getFormattedTime());
        timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        Label msgLbl = new Label(n.getMessage());
        msgLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
        msgLbl.setWrapText(true);

        card.getChildren().addAll(timeLbl, msgLbl);
        return card;
    }
}
