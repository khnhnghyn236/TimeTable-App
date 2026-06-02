package gui;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import main.AppSchedulerGUI;
import scheduling.Resource;
import scheduling.TimeSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class StaffDashboard {
    private AppSchedulerGUI app;
    private TabPane tabPane;
    private Tab gridTab;
    private GridPane grid;
    private ComboBox<Resource> roomCombo;
    private TextField titleField;
    private Spinner<Integer> capacitySpinner;

    private boolean[] isAddingMode = { true };

    public StaffDashboard(AppSchedulerGUI app) {
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
        Label titleLbl = new Label("Faculty Portal - " + app.currentStaff.getName());
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #D1E8FF;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #85B7EB; -fx-border-radius: 6; " +
                        "-fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 14;");
        logoutBtn.setOnAction(e -> {
            if (app.confirmAction("Confirm Log Out", "Are you sure you want to log out?")) {
                app.showLogin();
            }
        });

        header.getChildren().addAll(appName, sep, titleLbl, spacer, logoutBtn);

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        gridTab = new Tab("My Schedule Grid");

        // Initialize Room Combo Box
        roomCombo = new ComboBox<>(app.resourceCatalog);
        if (!app.resourceCatalog.isEmpty()) {
            roomCombo.setValue(app.resourceCatalog.get(0));
        }
        roomCombo.setCellFactory(param -> new ListCell<Resource>() {
            @Override
            protected void updateItem(Resource item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (Cap: " + item.getCapacity() + ")");
                }
            }
        });
        roomCombo.setButtonCell(new ListCell<Resource>() {
            @Override
            protected void updateItem(Resource item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        roomCombo.setOnAction(e -> gridTab.setContent(createGridTab()));

        titleField = new TextField("Meeting");
        titleField.setPromptText("Slot Title");
        titleField.setPrefWidth(120);

        capacitySpinner = new Spinner<>(1, 100, 1);
        capacitySpinner.setPrefWidth(65);

        gridTab.setContent(createGridTab());

        Tab listTab = new Tab("My Timeslots");
        listTab.setContent(createListTab());

        Tab notifyTab = new Tab("Notifications");
        notifyTab.setContent(createNotifyTab());

        tabPane.getTabs().addAll(gridTab, listTab, notifyTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        layout.getChildren().addAll(header, tabPane);
        return layout;
    }

    // --- Grid Tab ---
    private VBox createGridTab() {
        // STEP 1: Set up main container for Grid Tab
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + AppStyles.WHITE + "; -fx-background-radius: 8;");

        DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.US);
        Label weekLabel = new Label("Week of: " + app.currentWeekStart.format(weekFormatter));
        weekLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button prev = AppStyles.ghostButton("< Prev Week");
        Button next = AppStyles.ghostButton("Next Week >");

        prev.setOnAction(e -> {
            app.currentWeekStart = app.currentWeekStart.minusWeeks(1);
            gridTab.setContent(createGridTab());
        });
        next.setOnAction(e -> {
            app.currentWeekStart = app.currentWeekStart.plusWeeks(1);
            gridTab.setContent(createGridTab());
        });

        // STEP 2: Set up navigation controls for week switching
        HBox nav = new HBox(15, prev, weekLabel, next, new Label("Room:"), roomCombo, new Label("Title:"), titleField,
                new Label("Limit:"), capacitySpinner);
        nav.setAlignment(Pos.CENTER);

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setGridLinesVisible(true);

        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setPrefWidth(80);
        grid.getColumnConstraints().add(timeCol);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }

        // STEP 3: Initialize the schedule grid layout
        grid.setOnMouseReleased(e -> {
            long newSlotCount = app.systemTimeSlots.stream()
                    .filter(s -> s.getCreatorId().equals(app.currentStaff.getUserId())
                            && "PENDING".equals(s.getStatus()))
                    .count();
            app.normalizeTimeSlots();
            // Notify admins when faculty submits/updates slots
            if (newSlotCount > 0) {
                app.broadcastNotification("ADMINS",
                        "📋 Faculty " + app.currentStaff.getName() + " submitted timeslot changes for review.");
            }
            main.DataManager.saveState(app.systemTimeSlots);
            refreshGridAndList();
        });
        setupGrid(grid);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // STEP 4: Add a legend for slot status colors
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                createLegend("Pending", AppStyles.STATUS_PENDING),
                createLegend("Approved", AppStyles.STATUS_APPROVED),
                createLegend("Declined", AppStyles.STATUS_DECLINED));

        box.getChildren().addAll(nav, legend, scroll);
        return box;
    }

    private Label createLegend(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: " + color + "; -fx-text-fill: "
                + (color.equals(AppStyles.STATUS_PENDING) ? "black" : "white")
                + "; -fx-padding: 5 10 5 10; -fx-background-radius: 4; -fx-font-weight: bold;");
        return l;
    }

    private void setupGrid(GridPane grid) {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.US);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.US);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (int day = 0; day < 7; day++) {
            LocalDate date = app.currentWeekStart.plusDays(day);
            Label dayName = new Label(date.format(dayFormatter));
            dayName.setStyle("-fx-font-weight: bold; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");
            Label dateNum = new Label(date.format(dateFormatter));
            dateNum.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppStyles.BLUE_MID + ";");

            VBox headerBox = new VBox(2, dayName, dateNum);
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setPadding(new Insets(10));
            grid.add(headerBox, day + 1, 0);
        }

        for (int col = 0; col < 7; col++) {
            LocalDate date = app.currentWeekStart.plusDays(col);
            int row = 0;
            java.util.Set<TimeSlot> renderedSlots = new java.util.HashSet<>();
            while (row < 22) {
                LocalTime time = LocalTime.of(7, 0).plusMinutes(30 * row);

                Resource currentRoom = roomCombo.getValue();
                String roomName = currentRoom != null ? currentRoom.getName() : "";

                TimeSlot matchingSlot = app.systemTimeSlots.stream()
                        .filter(s -> s.getLocalDate().equals(date) &&
                                !s.getStartTime().isAfter(time) && s.getEndTime().isAfter(time) &&
                                s.getResource().getName().equals(roomName))
                        .findFirst().orElse(null);

                if (matchingSlot == null) {
                    matchingSlot = app.systemTimeSlots.stream()
                            .filter(s -> s.getLocalDate().equals(date) &&
                                    !s.getStartTime().isAfter(time) && s.getEndTime().isAfter(time) &&
                                    s.getCreatorId().equals(app.currentStaff.getUserId()))
                            .findFirst().orElse(null);
                }

                if (matchingSlot != null) {
                    int span = (int) Math.ceil((double) java.time.Duration
                            .between(matchingSlot.getStartTime(), matchingSlot.getEndTime()).toMinutes() / 30.0);
                    if (span <= 0)
                        span = 1;

                    if (!renderedSlots.contains(matchingSlot)) {
                        Button btn = createMergedSlotButton(matchingSlot, span, roomName);
                        grid.add(btn, col + 1, row + 1);
                        GridPane.setRowSpan(btn, span);
                        renderedSlots.add(matchingSlot);
                    }
                    row += span;
                } else {
                    if (col == 0) {
                        grid.add(new Label(" " + time.toString() + " "), 0, row + 1);
                    }

                    boolean isPast = date.isBefore(today) || (date.isEqual(today) && time.isBefore(now));
                    Button btn = createSlotButton(date, time, isPast);
                    grid.add(btn, col + 1, row + 1);
                    row++;
                }
            }
        }
    }

    // Create merged slot button
    private Button createMergedSlotButton(TimeSlot slot, int span, String currentRoomName) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMaxHeight(Double.MAX_VALUE);
        btn.setPrefSize(110, 45 * span);

        boolean isRoomConflict = !slot.getCreatorId().equals(app.currentStaff.getUserId());
        boolean isFacultyConflict = !isRoomConflict && !slot.getResource().getName().equals(currentRoomName);

        String bgColor;
        String fgColor = "white";
        String graphicText = slot.getStartTime() + " - " + slot.getEndTime();

        if (isRoomConflict) {
            bgColor = "#EF4444";
            graphicText = "❌\nBooked by Other";
        } else if (isFacultyConflict) {
            bgColor = "#EF4444";
            graphicText = "❌\nBooked by You";
        } else if (slot.getStatus().equals("APPROVED")) {
            bgColor = AppStyles.STATUS_APPROVED;
        } else if (slot.getStatus().equals("DECLINED")) {
            bgColor = AppStyles.STATUS_DECLINED;
        } else {
            bgColor = AppStyles.STATUS_PENDING;
            fgColor = "black";
        }

        btn.setText(graphicText);
        String style = "-fx-background-color: " + bgColor + "; -fx-text-fill: " + fgColor
                + "; -fx-border-color: white; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center;";
        if (isRoomConflict) {
            style += " -fx-font-size: 12px; -fx-opacity: 0.5;";
        } else if (isFacultyConflict) {
            style += " -fx-font-size: 12px; -fx-opacity: 0.5;";
        }
        btn.setStyle(style);

        if (!isRoomConflict && !isFacultyConflict) {
            btn.setOnDragDetected(e -> {
                btn.startFullDrag();
                isAddingMode[0] = false;
                toggleRange(slot.getLocalDate(), slot.getStartTime(), slot.getEndTime());
            });
            btn.setOnMouseDragEntered(e -> {
                isAddingMode[0] = false;
                toggleRange(slot.getLocalDate(), slot.getStartTime(), slot.getEndTime());
            });
            btn.setOnAction(e -> {
                isAddingMode[0] = false;
                toggleRange(slot.getLocalDate(), slot.getStartTime(), slot.getEndTime());
                refreshGridAndList();
            });
        }
        return btn;
    }

    // Create normal slot button
    private Button createSlotButton(LocalDate date, LocalTime time, boolean isPast) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMaxHeight(Double.MAX_VALUE);
        btn.setPrefSize(110, 45);

        String baseColor = isPast ? "#E2E8F0" : "transparent";
        btn.setStyle("-fx-background-color: " + baseColor + "; -fx-border-color: " + AppStyles.STATUS_DEFAULT + ";");

        btn.setOnDragDetected(e -> {
            if (roomCombo.getValue() == null)
                return;
            btn.startFullDrag();
            isAddingMode[0] = true;
            toggleSingle(date, time, time.plusMinutes(30));
            btn.setStyle("-fx-background-color: " + AppStyles.STATUS_PENDING + ";");
        });
        btn.setOnMouseDragEntered(e -> {
            if (roomCombo.getValue() == null)
                return;
            if (isAddingMode[0]) {
                toggleSingle(date, time, time.plusMinutes(30));
                btn.setStyle("-fx-background-color: " + AppStyles.STATUS_PENDING + ";");
            }
        });
        btn.setOnAction(e -> {
            if (roomCombo.getValue() == null) {
                app.showAlert(Alert.AlertType.ERROR, "Room Selection", "Please select a room first.");
                return;
            }
            isAddingMode[0] = true;
            toggleSingle(date, time, time.plusMinutes(30));
            app.normalizeTimeSlots();
            refreshGridAndList();
        });
        return btn;
    }

    // Refresh grid and list --> update the window according to new time slots
    private void refreshGridAndList() {
        gridTab.setContent(createGridTab());
        int selIndex = tabPane.getSelectionModel().getSelectedIndex();
        tabPane.getTabs().set(1, new Tab("My Timeslots", createListTab()));
        if (selIndex == 1) {
            tabPane.getSelectionModel().select(1);
        }
    }

    // Toggle range of time slots --> used when dragging
    private void toggleRange(LocalDate date, LocalTime start, LocalTime end) {
        LocalTime time = start;
        while (time.isBefore(end)) {
            toggleSingle(date, time, time.plusMinutes(30));
            time = time.plusMinutes(30);
        }
    }

    // Toggle single time slot --> used when clicking
    private void toggleSingle(LocalDate date, LocalTime start, LocalTime end) {
        Resource selectedRoom = roomCombo.getValue();
        if (selectedRoom == null)
            return;

        if (isAddingMode[0]) {
            boolean exists = app.systemTimeSlots.stream().anyMatch(s -> s.getLocalDate().equals(date) &&
                    s.overlaps(date, start, end) &&
                    s.getResource().getName().equals(selectedRoom.getName()) &&
                    s.getCreatorId().equals(app.currentStaff.getUserId()));

            boolean facultyConflict = app.systemTimeSlots.stream().anyMatch(s -> s.getLocalDate().equals(date) &&
                    s.overlaps(date, start, end) &&
                    s.getCreatorId().equals(app.currentStaff.getUserId()) &&
                    !s.getResource().getName().equals(selectedRoom.getName()));

            boolean roomConflict = app.systemTimeSlots.stream().anyMatch(s -> s.getLocalDate().equals(date) &&
                    s.overlaps(date, start, end) &&
                    s.getResource().getName().equals(selectedRoom.getName()) &&
                    !s.getCreatorId().equals(app.currentStaff.getUserId()));

            if (!exists && !facultyConflict && !roomConflict) {
                String range = date + " | " + start + " - " + end;
                String customTitle = titleField.getText().trim();
                if (customTitle.isEmpty())
                    customTitle = "Meeting";
                TimeSlot slot = new TimeSlot(range, selectedRoom, app.currentStaff.getUserId(),
                        app.currentStaff.getName(), "PENDING", customTitle, capacitySpinner.getValue());
                app.systemTimeSlots.add(slot);
            }
        } else {
            app.systemTimeSlots.removeIf(s -> s.getLocalDate().equals(date) &&
                    s.overlaps(date, start, end) &&
                    s.getResource().getName().equals(selectedRoom.getName()) &&
                    s.getCreatorId().equals(app.currentStaff.getUserId()));
        }
        main.DataManager.saveState(app.systemTimeSlots);
    }

    // --- Notifications Tab ---
    private VBox createNotifyTab() {
        // STEP 1: Set up main layout for Notifications tab
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");

        Label lbl = AppStyles.headerLabel("My Notifications");

        javafx.collections.ObservableList<VBox> items = javafx.collections.FXCollections.observableArrayList();

        // STEP 2: Filter system notifications for the current staff member
        java.util.List<scheduling.Notification> myNotifs = new java.util.ArrayList<>();
        for (scheduling.Notification n : app.systemNotifications) {
            if (n.getUserId().equals(app.currentStaff.getUserId())) {
                myNotifs.add(n);
            }
        }
        java.util.Collections.reverse(myNotifs); // Newest first

        for (scheduling.Notification n : myNotifs) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(10, 14, 10, 14));
            card.setStyle(
                    "-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");

            Label timeLbl = new Label(n.getFormattedTime());
            timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
            Label msgLbl = new Label(n.getMessage());
            msgLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
            msgLbl.setWrapText(true);

            card.getChildren().addAll(timeLbl, msgLbl);
            items.add(card);
        }

        ListView<VBox> list = new ListView<>(items);
        list.setPlaceholder(new Label("No notifications yet."));
        list.setStyle("-fx-background-color: " + AppStyles.BG_PAGE + ";");
        VBox.setVgrow(list, Priority.ALWAYS);

        Label countLbl = new Label(myNotifs.size() + " message(s)");
        countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.TEXT_MUTED + ";");

        HBox topRow = new HBox(10, lbl, new javafx.scene.layout.Region(), countLbl);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button clearBtn = AppStyles.dangerButton("Clear All Notifications");
        clearBtn.setPrefHeight(36);
        clearBtn.setOnAction(e -> {
            if (app.confirmAction("Clear Notifications", "Remove all your notifications?")) {
                app.clearNotifications(app.currentStaff.getUserId());
                items.clear();
            }
        });

        box.getChildren().addAll(topRow, list, clearBtn);
        return box;
    }

    // --- My Timeslots List Tab ---
    private VBox createListTab() {
        // STEP 1: Set up main layout for List Tab
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label lbl = AppStyles.headerLabel("My Timeslots (List View)");

        // STEP 2: Display list of timeslots created by the staff member
        ListView<HBox> list = new ListView<>();
        for (TimeSlot slot : app.systemTimeSlots) {
            if (slot.getCreatorId().equals(app.currentStaff.getUserId())) {
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10));
                row.setStyle(
                        "-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");

                VBox info = new VBox(5);
                Label titleLbl = new Label(slot.getTitle() + " - " + slot.getResource().getName());
                titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                Label timeLbl = new Label(
                        slot.getLocalDate() + " | " + slot.getStartTime() + " - " + slot.getEndTime());
                timeLbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
                Label statusLbl = new Label(
                        "Status: " + slot.getStatus() + " | Booked: " + slot.getConfirmedStudents().size());
                statusLbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
                info.getChildren().addAll(titleLbl, timeLbl, statusLbl);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // STEP 3: Implement Edit and Delete actions for each timeslot
                Button editBtn = AppStyles.ghostButton("Edit");
                editBtn.setOnAction(e -> {
                    javafx.stage.Stage editStage = new javafx.stage.Stage();
                    editStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    editStage.setTitle("Edit Timeslot");
                    editStage.setMaximized(true);

                    GridPane editGrid = new GridPane();
                    editGrid.setPadding(new Insets(20));
                    editGrid.setHgap(10);
                    editGrid.setVgap(15);
                    editGrid.setAlignment(Pos.CENTER);

                    DatePicker datePicker = new DatePicker(slot.getLocalDate());

                    HBox timeBox = new HBox(5);
                    timeBox.setAlignment(Pos.CENTER_LEFT);
                    TextField startFld = new TextField(slot.getStartTime().toString());
                    startFld.setPrefWidth(70);
                    TextField endFld = new TextField(slot.getEndTime().toString());
                    endFld.setPrefWidth(70);
                    timeBox.getChildren().addAll(startFld, new Label("-"), endFld);

                    TextField titleFld = new TextField(slot.getTitle());
                    ComboBox<Resource> roomCb = new ComboBox<>(app.resourceCatalog);
                    Resource matchedRes = app.resourceCatalog.stream()
                            .filter(r -> r.getName().equals(slot.getResource().getName()))
                            .findFirst().orElse(slot.getResource());
                    roomCb.setValue(matchedRes);

                    roomCb.setConverter(new javafx.util.StringConverter<Resource>() {
                        @Override
                        public String toString(Resource object) {
                            return object == null ? null : object.getName();
                        }

                        @Override
                        public Resource fromString(String string) {
                            return null;
                        }
                    });

                    roomCb.setCellFactory(param -> new ListCell<Resource>() {
                        @Override
                        protected void updateItem(Resource item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null
                                    : item.getName() + " (Cap: " + item.getCapacity() + ")");
                        }
                    });

                    TextField capFld = new TextField(String.valueOf(slot.getSlotCapacity()));

                    editGrid.add(new Label("Date:"), 0, 0);
                    editGrid.add(datePicker, 1, 0);
                    editGrid.add(new Label("Time (HH:MM):"), 0, 1);
                    editGrid.add(timeBox, 1, 1);
                    editGrid.add(new Label("Title:"), 0, 2);
                    editGrid.add(titleFld, 1, 2);
                    editGrid.add(new Label("Room:"), 0, 3);
                    editGrid.add(roomCb, 1, 3);
                    editGrid.add(new Label("Capacity:"), 0, 4);
                    editGrid.add(capFld, 1, 4);

                    Button cancelBtn = new Button("Cancel");
                    cancelBtn.setStyle(
                            "-fx-background-color: " + AppStyles.TEXT_MUTED
                                    + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
                    cancelBtn.setOnAction(evt -> editStage.close());

                    Button confirmBtn = new Button("Confirm change");
                    confirmBtn.setStyle(
                            "-fx-background-color: " + AppStyles.STATUS_DEFAULT
                                    + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
                    confirmBtn.setDisable(true);

                    Runnable validate = () -> {
                        String t = titleFld.getText().trim();
                        String cStr = capFld.getText().trim();
                        String sStr = startFld.getText().trim();
                        String eStr = endFld.getText().trim();
                        java.time.LocalDate newDate = datePicker.getValue();
                        Resource r = roomCb.getValue();

                        boolean blank = t.isEmpty() || cStr.isEmpty() || sStr.isEmpty() || eStr.isEmpty() || r == null
                                || newDate == null;
                        if (blank) {
                            confirmBtn.setDisable(true);
                            confirmBtn.setStyle(
                                    "-fx-background-color: " + AppStyles.STATUS_DEFAULT
                                            + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
                            return;
                        }

                        int c = -1;
                        java.time.LocalTime sTime, eTime;
                        try {
                            c = Integer.parseInt(cStr);
                            sTime = java.time.LocalTime.parse(sStr);
                            eTime = java.time.LocalTime.parse(eStr);
                            if (!eTime.isAfter(sTime))
                                throw new Exception();
                        } catch (Exception ex) {
                            confirmBtn.setDisable(true);
                            confirmBtn.setStyle(
                                    "-fx-background-color: " + AppStyles.STATUS_DEFAULT
                                            + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
                            return;
                        }

                        if (c <= 0 || c < slot.getConfirmedStudents().size()) {
                            confirmBtn.setDisable(true);
                            confirmBtn.setStyle(
                                    "-fx-background-color: " + AppStyles.STATUS_DEFAULT
                                            + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
                            return;
                        }

                        String oldTitle = slot.getTitle() == null ? "" : slot.getTitle().trim();
                        boolean changed = !t.equals(oldTitle) || !r.getName().equals(slot.getResource().getName())
                                || c != slot.getSlotCapacity()
                                || !newDate.equals(slot.getLocalDate()) || !sTime.equals(slot.getStartTime())
                                || !eTime.equals(slot.getEndTime());
                        if (changed) {
                            confirmBtn.setDisable(false);
                            confirmBtn.setStyle(
                                    "-fx-background-color: " + AppStyles.ERROR_RED
                                            + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
                        } else {
                            confirmBtn.setDisable(true);
                            confirmBtn.setStyle(
                                    "-fx-background-color: " + AppStyles.STATUS_DEFAULT
                                            + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 8 15;");
                        }
                    };

                    titleFld.textProperty().addListener((o, oldV, newV) -> validate.run());
                    capFld.textProperty().addListener((o, oldV, newV) -> validate.run());
                    roomCb.valueProperty().addListener((o, oldV, newV) -> validate.run());
                    datePicker.valueProperty().addListener((o, oldV, newV) -> validate.run());
                    startFld.textProperty().addListener((o, oldV, newV) -> validate.run());
                    endFld.textProperty().addListener((o, oldV, newV) -> validate.run());

                    confirmBtn.setOnAction(evt -> {
                        if (app.confirmAction("Confirm Edit", "Are you sure you want to save these changes?")) {
                            String newTitle = titleFld.getText().trim();
                            Resource newRoom = roomCb.getValue();
                            int newCap = Integer.parseInt(capFld.getText().trim());
                            java.time.LocalDate newDate = datePicker.getValue();
                            java.time.LocalTime newStart = java.time.LocalTime.parse(startFld.getText().trim());
                            java.time.LocalTime newEnd = java.time.LocalTime.parse(endFld.getText().trim());

                            TimeSlot conflictingSlot = app.systemTimeSlots
                                    .stream().filter(s -> s != slot && s.overlaps(newDate, newStart, newEnd) &&
                                            (s.getResource().getName().equals(newRoom.getName())
                                                    || s.getCreatorId().equals(app.currentStaff.getUserId())))
                                    .findFirst().orElse(null);

                            if (conflictingSlot != null) {
                                if (conflictingSlot.getResource().getName().equals(newRoom.getName())) {
                                    if (conflictingSlot.getCreatorId().equals(app.currentStaff.getUserId())) {
                                        app.showAlert(Alert.AlertType.ERROR, "Conflict",
                                                "You already have another slot booked in this room at the same time: "
                                                        + conflictingSlot.getTitle());
                                    } else {
                                        app.showAlert(Alert.AlertType.ERROR, "Conflict",
                                                "This room is already booked at this time by "
                                                        + conflictingSlot.getCreatorName() + " ("
                                                        + conflictingSlot.getTitle() + ")");
                                    }
                                } else {
                                    app.showAlert(Alert.AlertType.ERROR, "Conflict",
                                            "You already have a slot booked in another room at this time: "
                                                    + conflictingSlot.getResource().getName() + " ("
                                                    + conflictingSlot.getTitle() + ")");
                                }
                            } else {
                                slot.setTimeRange(
                                        newDate.toString() + " | " + newStart.toString() + " - " + newEnd.toString());
                                slot.setTitle(newTitle);
                                slot.setResource(newRoom);
                                slot.setSlotCapacity(newCap);
                                app.showAlert(Alert.AlertType.INFORMATION, "Success", "Timeslot updated.");
                                main.DataManager.saveState(app.systemTimeSlots);
                                int selIndex = tabPane.getSelectionModel().getSelectedIndex();
                                tabPane.getTabs().set(1, new Tab("My Timeslots", createListTab()));
                                gridTab.setContent(createGridTab());
                                if (selIndex == 1) {
                                    tabPane.getSelectionModel().select(1);
                                }
                                editStage.close();
                            }
                        }
                    });

                    HBox buttons = new HBox(15, cancelBtn, confirmBtn);
                    buttons.setAlignment(Pos.CENTER_RIGHT);
                    buttons.setPadding(new Insets(10, 0, 0, 0));
                    editGrid.add(buttons, 0, 5, 2, 1);

                    javafx.scene.Scene scene = new javafx.scene.Scene(editGrid, 400, 320);
                    editStage.setScene(scene);
                    editStage.showAndWait();
                });
                Button delBtn = AppStyles.dangerButton("Delete");
                delBtn.setOnAction(e -> {
                    if (app.confirmAction("Delete Timeslot", "Are you sure you want to delete this timeslot?")) {
                        app.systemTimeSlots.remove(slot);
                        main.DataManager.saveState(app.systemTimeSlots);
                        tabPane.getTabs().set(1, new Tab("My Timeslots", createListTab()));
                        gridTab.setContent(createGridTab());
                    }
                });

                row.getChildren().addAll(info, spacer, editBtn, delBtn);
                list.getItems().add(row);
            }
        }
        VBox.setVgrow(list, Priority.ALWAYS);

        box.getChildren().addAll(lbl, list);
        return box;
    }
}