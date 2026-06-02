package main;

import javafx.application.Application;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import gui.*;
import users.*;
import scheduling.*;
import datastructures.*;
import java.time.*;
import java.util.*;
import scheduling.Notification;
import java.util.Comparator;

public class AppSchedulerGUI extends Application {
    private static final String[][] DEFAULT_ADMINS = {
            { "V202502310", "Nguyen Hong Khanh", "khanh@vinuni.edu" },
            { "V202502059", "Truong Ba Ky", "ky@vinuni.edu" },
            { "V202502448", "Nguyen Trong Nguyen", "nguyen@vinuni.edu" },
            { "V202502393", "Tran Trong Tai", "tai@vinuni.edu" }
    };

    public Stage window;

    public List<TimeSlot> systemTimeSlots = new ArrayList<>();
    public List<Notification> systemNotifications = new ArrayList<>();
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
        // STEP 1: Set up the primary application window
        this.window = primaryStage;
        window.setTitle("TimeTable - Appointment Scheduling Platform");

        // STEP 2: Load persisted data (users, resources, appointments, notifications)
        loadUsersFromStorage();
        loadResourcesFromStorage();
        loadAppointmentsFromStorage();
        systemNotifications.addAll(DataManager.loadNotifications());
        syncResourcesFromSlots();
        updateStudentUIList();

        // STEP 3: Display the initial login screen
        showLogin();
        window.show();
    }

    private void loadUsersFromStorage() {
        // STEP 4: Initialize user data and ensure default admins exist
        allUsers = DataManager.loadUsers();
        if (allUsers.isEmpty()) {
            createDefaultUsers();
            DataManager.saveUsers(allUsers);
        } else if (ensureDefaultAdmins()) {
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

    private void loadAppointmentsFromStorage() {
        systemTimeSlots = DataManager.loadState(allUsers);
    }

    private void syncResourcesFromSlots() {
        for (TimeSlot slot : systemTimeSlots) {
            addResourceIfMissing(slot.getResource(), false);
        }
        DataManager.saveResources(new ArrayList<>(resourceCatalog));
    }

    private void createDefaultUsers() {
        for (String[] admin : DEFAULT_ADMINS) {
            allUsers.add(createDefaultAdmin(admin));
        }
    }

    private boolean ensureDefaultAdmins() {
        boolean changed = false;
        for (String[] adminInfo : DEFAULT_ADMINS) {
            int existingIndex = findUserIndexById(adminInfo[0]);
            Administrator defaultAdmin = createDefaultAdmin(adminInfo);
            if (existingIndex == -1) {
                allUsers.add(defaultAdmin);
                changed = true;
            } else {
                User existingUser = allUsers.get(existingIndex);
                boolean needsUpdate = !(existingUser instanceof Administrator)
                        || !existingUser.getName().equals(defaultAdmin.getName())
                        || !existingUser.getEmail().equals(defaultAdmin.getEmail())
                        || !existingUser.verifyPassword("admin123")
                        || !existingUser.isApproved();
                if (needsUpdate) {
                    allUsers.set(existingIndex, defaultAdmin);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private int findUserIndexById(String userId) {
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUserId().equalsIgnoreCase(userId))
                return i;
        }
        return -1;
    }

    private Administrator createDefaultAdmin(String[] adminInfo) {
        Administrator admin = new Administrator(adminInfo[0], adminInfo[1], adminInfo[2], "admin123");
        admin.setApproved(true);
        return admin;
    }

    public void registerNewUser(User user) {
        allUsers.add(user);
        rebuildUserDatabase();
        rebuildPendingUsers();
        DataManager.saveUsers(allUsers);
        broadcastNotification("ADMINS", "New account request from " + user.getName() + " (" + user.getUserId() + ").");
        System.out.println("Registered new user for approval: " + user.getName());
    }

    public void approveUser(User user) {
        // STEP 5: Handle user approval logic and send welcome notification
        user.setApproved(true);
        rebuildPendingUsers();
        DataManager.saveUsers(allUsers);
        if (user instanceof users.Student) {
            addNotification(user.getUserId(),
                    "🎉 Welcome, " + user.getName() + "! Your account has been approved.\n" +
                            "You can now browse available appointment slots under 'Book Appointments', " +
                            "and track your bookings under 'Manage Schedules'.");
        } else if (user instanceof users.AcademicStaff) {
            addNotification(user.getUserId(),
                    "🎉 Welcome, " + user.getName() + "! Your faculty account has been approved.\n" +
                            "Head to the 'Schedule Grid' to select a room and block out your availability. " +
                            "Submitted timeslots will be reviewed by an Admin before going live.");
        }
    }

    public void declineUser(User user) {
        allUsers.remove(user);
        rebuildUserDatabase();
        rebuildPendingUsers();
        DataManager.saveUsers(allUsers);
    }

    public boolean emailExists(String email) {
        for (User user : allUsers) {
            if (user.getEmail().equalsIgnoreCase(email))
                return true;
        }
        return false;
    }

    public void rebuildUserDatabase() {
        userDatabase = new CustomHashMap<>();
        for (User user : allUsers)
            userDatabase.put(user.getUserId(), user);
    }

    public void rebuildPendingUsers() {
        pendingUsers.clear();
        for (User user : allUsers) {
            if (!(user instanceof Administrator) && !user.isApproved())
                pendingUsers.add(user);
        }
    }

    public boolean addResource(Resource resource) {
        boolean added = addResourceIfMissing(resource, true);
        if (added) {
            DataManager.saveResources(new ArrayList<>(resourceCatalog));
            broadcastNotification("ALL", "Admin created a new bookable room: " + resource.getName() + " (Capacity: "
                    + resource.getCapacity() + ").");
        }
        return added;
    }

    private boolean addResourceIfMissing(Resource resource, boolean updateActive) {
        for (Resource existing : resourceCatalog) {
            if (existing.getName().equalsIgnoreCase(resource.getName()))
                return false;
        }
        resourceCatalog.add(resource);
        if (updateActive)
            activeResource = resource;
        return true;
    }

    public boolean resourceNameExists(String name) {
        for (Resource resource : resourceCatalog) {
            if (resource.getName().equalsIgnoreCase(name.trim()))
                return true;
        }
        return false;
    }

    public boolean slotExists(Resource resource, String timeRange) {
        for (TimeSlot slot : systemTimeSlots) {
            if (slot.getResource().equals(resource) && slot.getTimeRange().equals(timeRange))
                return true;
        }
        return false;
    }

    public TimeSlot findSlot(Resource resource, String timeRange) {
        for (TimeSlot slot : systemTimeSlots) {
            if (slot.getResource().equals(resource) && slot.getTimeRange().equals(timeRange))
                return slot;
        }
        return null;
    }

    public Student findApprovedStudent(String idOrEmail) {
        if (idOrEmail == null)
            return null;
        String query = idOrEmail.trim();
        if (query.isEmpty())
            return null;
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
        if (systemTimeSlots.isEmpty())
            return;

        CustomBST<TimeSlot> bst = new CustomBST<>();
        for (TimeSlot slot : systemTimeSlots)
            bst.insert(slot);

        List<TimeSlot> sortedSlots = bst.getSortedList();
        for (TimeSlot slot : sortedSlots) {
            uiSlotList.add(slot.getSummary());
        }
    }

    public void switchScreen(Scene scene) {
        // STEP 6: Helper method to switch views while maintaining window bounds
        window.setScene(scene);
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        window.setWidth(bounds.getWidth());
        window.setHeight(bounds.getHeight());
        window.setX(bounds.getMinX());
        window.setY(bounds.getMinY());
    }

    public void showLogin() {
        switchScreen(new Scene(new LoginScreen(this).getContent()));
    }

    public void showStudent() {
        switchScreen(new Scene(new StudentDashboard(this).getContent()));
    }

    public void showAdmin() {
        switchScreen(new Scene(new AdminDashboard(this).getContent()));
    }

    public void showStaff() {
        switchScreen(new Scene(new StaffDashboard(this).getContent()));
    }

    public void showSignUp() {
        switchScreen(new Scene(new SignUpScreen(this).getContent()));
    }

    public void addNotification(String userId, String message) {
        systemNotifications.add(new Notification(userId, message));
        DataManager.saveNotifications(systemNotifications);
    }

    public void broadcastNotification(String targetGroup, String message) {
        if ("ADMINS".equals(targetGroup)) {
            for (User u : allUsers) {
                if (u instanceof Administrator)
                    addNotification(u.getUserId(), message);
            }
        } else if ("ALL".equals(targetGroup)) {
            for (User u : allUsers) {
                if (u.isApproved())
                    addNotification(u.getUserId(), message);
            }
        }
    }

    public void clearNotifications(String userId) {
        systemNotifications.removeIf(n -> n.getUserId().equals(userId));
        DataManager.saveNotifications(systemNotifications);
    }

    public void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void normalizeTimeSlots() {
        if (systemTimeSlots.isEmpty())
            return;
        systemTimeSlots.sort(Comparator.comparing(TimeSlot::getLocalDate)
                .thenComparing(s -> s.getResource().getName())
                .thenComparing(TimeSlot::getCreatorId)
                .thenComparing(TimeSlot::getStartTime));

        List<TimeSlot> merged = new ArrayList<>();
        TimeSlot current = systemTimeSlots.get(0);

        for (int i = 1; i < systemTimeSlots.size(); i++) {
            TimeSlot next = systemTimeSlots.get(i);
            if (current.getLocalDate().equals(next.getLocalDate()) &&
                    current.getResource().getName().equals(next.getResource().getName()) &&
                    current.getCreatorId().equals(next.getCreatorId()) &&
                    current.getTitle().equals(next.getTitle()) &&
                    (current.getEndTime().equals(next.getStartTime())
                            || current.getEndTime().isAfter(next.getStartTime()))) {

                if (next.getEndTime().isAfter(current.getEndTime())) {
                    current.setTimeRange(
                            current.getLocalDate() + " | " + current.getStartTime() + " - " + next.getEndTime());
                }
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        systemTimeSlots = merged;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
