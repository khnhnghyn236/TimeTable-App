package main;

import javafx.application.Application;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import gui.*;
import users.*;
import scheduling.*;
import datastructures.*;
import java.time.*;
import java.util.*;

public class AppSchedulerGUI extends Application {
    private Stage window;

    public List<TimeSlot> systemTimeSlots = new ArrayList<>();
    public ObservableList<String> uiSlotList = FXCollections.observableArrayList();
    public Resource activeResource = new Resource("General Office", 2);

    public CustomHashMap<String, User> userDatabase = new CustomHashMap<>();
    public ObservableList<User> pendingUsers = FXCollections.observableArrayList();
    public List<User> allUsers = new ArrayList<>();

    public Student currentStudent;
    public AcademicStaff currentStaff;
    public Administrator currentAdmin;

    public LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("TimeTable - Appointment Scheduling Platform");

        loadUsersFromStorage();

        systemTimeSlots = DataManager.loadState();
        updateStudentUIList();

        showLogin();
        window.show();
    }

    private void loadUsersFromStorage() {
        allUsers = DataManager.loadUsers();
        if (allUsers.isEmpty()) {
            createDefaultUsers();
            DataManager.saveUsers(allUsers);
        }
        rebuildUserDatabase();
        rebuildPendingUsers();
    }

    private void createDefaultUsers() {
        Student testStudent = new Student("V202502059", "Truong Ba Ky", "ky@vinuni.edu", "pass123");
        testStudent.setApproved(true);
        AcademicStaff testStaff = new AcademicStaff("P001", "Prof. Bob", "bob@vinuni.edu", "pass123");
        testStaff.setApproved(true);
        Administrator testAdmin = new Administrator("A001", "Admin Alice", "alice@vinuni.edu", "admin123");
        testAdmin.setApproved(true);
        allUsers.add(testStudent);
        allUsers.add(testStaff);
        allUsers.add(testAdmin);
    }

    public void registerNewUser(User user) {
        allUsers.add(user);
        rebuildUserDatabase();
        rebuildPendingUsers();
        DataManager.saveUsers(allUsers);
    }

    public void approveUser(User user) {
        user.setApproved(true);
        rebuildPendingUsers();
        DataManager.saveUsers(allUsers);
    }

    public void declineUser(User user) {
        allUsers.remove(user);
        rebuildUserDatabase();
        rebuildPendingUsers();
        DataManager.saveUsers(allUsers);
    }

    public boolean emailExists(String email) {
        for (User user : allUsers) {
            if (user.getEmail().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    public void rebuildUserDatabase() {
        userDatabase = new CustomHashMap<>();
        for (User user : allUsers) userDatabase.put(user.getUserId(), user);
    }

    public void rebuildPendingUsers() {
        pendingUsers.clear();
        for (User user : allUsers) {
            if (!(user instanceof Administrator) && !user.isApproved()) pendingUsers.add(user);
        }
    }

    public boolean confirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void updateStudentUIList() {
        uiSlotList.clear();
        if (systemTimeSlots.isEmpty()) return;

        CustomBST<TimeSlot> bst = new CustomBST<>();
        for (TimeSlot slot : systemTimeSlots) bst.insert(slot);

        List<TimeSlot> sortedSlots = bst.getSortedList();
        String currentDate = null, currentStart = null, currentEnd = null;

        for (TimeSlot slot : sortedSlots) {
            String[] parts = slot.getTimeRange().split(" \\| ");
            if (parts.length < 2) continue;
            String datePart = parts[0];
            String[] times = parts[1].split(" - ");
            if (times.length < 2) continue;
            String start = times[0], end = times[1];

            if (currentDate == null) {
                currentDate = datePart; currentStart = start; currentEnd = end;
            } else if (currentDate.equals(datePart) && currentEnd.equals(start)) {
                currentEnd = end;
            } else {
                uiSlotList.add(currentDate + " | " + currentStart + " - " + currentEnd);
                currentDate = datePart; currentStart = start; currentEnd = end;
            }
        }
        if (currentDate != null) uiSlotList.add(currentDate + " | " + currentStart + " - " + currentEnd);
    }

    public void switchScreen(Scene scene) {
        window.setScene(scene);
        window.setWidth(Double.NaN);
        window.setHeight(Double.NaN);
        window.sizeToScene();
        window.centerOnScreen();
    }

    public void showLogin() { switchScreen(new Scene(new LoginScreen(this).getContent(), 760, 520)); }
    public void showStudent() { switchScreen(new Scene(new StudentDashboard(this).getContent(), 500, 500)); }
    public void showAdmin() { switchScreen(new Scene(new AdminDashboard(this).getContent(), 600, 460)); }
    public void showStaff() { switchScreen(new Scene(new StaffDashboard(this).getContent(), 950, 750)); }
    public void showSignUp() { switchScreen(new Scene(new SignUpScreen(this).getContent(), 450, 540)); }

    public static void main(String[] args) { launch(args); }
}
