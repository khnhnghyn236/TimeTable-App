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
    public ObservableList<Resource> resourceCatalog = FXCollections.observableArrayList();
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
        loadResourcesFromStorage();

        systemTimeSlots = DataManager.loadState(allUsers);
        syncResourcesFromSlots();
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

    private void loadResourcesFromStorage() {
        List<Resource> savedResources = DataManager.loadResources();
        resourceCatalog.clear();
        if (savedResources.isEmpty()) {
            resourceCatalog.add(new Resource("General Office", 2));
            resourceCatalog.add(new Resource("Library Study Room A", 4));
            resourceCatalog.add(new Resource("Consultation Room B", 2));
            DataManager.saveResources(new ArrayList<>(resourceCatalog));
        } else {
            resourceCatalog.addAll(savedResources);
        }
        activeResource = resourceCatalog.get(0);
    }

    private void syncResourcesFromSlots() {
        for (TimeSlot slot : systemTimeSlots) {
            addResourceIfMissing(slot.getResource(), false);
        }
        DataManager.saveResources(new ArrayList<>(resourceCatalog));
    }

    private void createDefaultUsers() {
        Administrator admin1 = new Administrator("V202502310", "Nguyen Hong Khanh", "khanh@vinuni.edu", "admin123");
        Administrator admin2 = new Administrator("V202502059", "Truong Ba Ky", "ky@vinuni.edu", "admin123");
        Administrator admin3 = new Administrator("V202502448", "Nguyen Trong Nguyen", "nguyen@vinuni.edu", "admin123");
        Administrator admin4 = new Administrator("V202502393", "Tran Trong Tai", "tai@vinuni.edu", "admin123");

        admin1.setApproved(true);
        admin2.setApproved(true);
        admin3.setApproved(true);
        admin4.setApproved(true);

        allUsers.add(admin1);
        allUsers.add(admin2);
        allUsers.add(admin3);
        allUsers.add(admin4);
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

    public boolean addResource(Resource resource) {
        boolean added = addResourceIfMissing(resource, true);
        if (added) DataManager.saveResources(new ArrayList<>(resourceCatalog));
        return added;
    }

    private boolean addResourceIfMissing(Resource resource, boolean updateActive) {
        for (Resource existing : resourceCatalog) {
            if (existing.getName().equalsIgnoreCase(resource.getName())) return false;
        }
        resourceCatalog.add(resource);
        if (updateActive) activeResource = resource;
        return true;
    }

    public boolean resourceNameExists(String name) {
        for (Resource resource : resourceCatalog) {
            if (resource.getName().equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    public boolean slotExists(Resource resource, String timeRange) {
        for (TimeSlot slot : systemTimeSlots) {
            if (slot.getResource().equals(resource) && slot.getTimeRange().equals(timeRange)) return true;
        }
        return false;
    }

    public TimeSlot findSlot(Resource resource, String timeRange) {
        for (TimeSlot slot : systemTimeSlots) {
            if (slot.getResource().equals(resource) && slot.getTimeRange().equals(timeRange)) return slot;
        }
        return null;
    }

    public Student findApprovedStudent(String idOrEmail) {
        if (idOrEmail == null) return null;
        String query = idOrEmail.trim();
        if (query.isEmpty()) return null;
        for (User user : allUsers) {
            if (user instanceof Student && user.isApproved()
                    && (user.getUserId().equalsIgnoreCase(query) || user.getEmail().equalsIgnoreCase(query))) {
                return (Student) user;
            }
        }
        return null;
    }

    public void saveAllBookingData() {
        DataManager.saveState(systemTimeSlots);
        DataManager.saveResources(new ArrayList<>(resourceCatalog));
        updateStudentUIList();
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
        for (TimeSlot slot : sortedSlots) {
            uiSlotList.add(slot.getSummary());
        }
    }

    public void switchScreen(Scene scene) {
        window.setScene(scene);
        window.setWidth(Double.NaN);
        window.setHeight(Double.NaN);
        window.sizeToScene();
        window.centerOnScreen();
    }

    public void showLogin() { switchScreen(new Scene(new LoginScreen(this).getContent(), 760, 520)); }
    public void showStudent() { switchScreen(new Scene(new StudentDashboard(this).getContent(), 780, 560)); }
    public void showAdmin() { switchScreen(new Scene(new AdminDashboard(this).getContent(), 760, 520)); }
    public void showStaff() { switchScreen(new Scene(new StaffDashboard(this).getContent(), 1000, 760)); }
    public void showSignUp() { switchScreen(new Scene(new SignUpScreen(this).getContent(), 450, 540)); }

    public static void main(String[] args) { launch(args); }
}
