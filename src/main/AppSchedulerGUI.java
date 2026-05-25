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
    private static final String[][] DEFAULT_ADMINS = {
            {"V202502310", "Nguyen Hong Khanh", "khanh@vinuni.edu"},
            {"V202502059", "Truong Ba Ky", "ky@vinuni.edu"},
            {"V202502448", "Nguyen Trong Nguyen", "nguyen@vinuni.edu"},
            {"V202502393", "Tran Trong Tai", "tai@vinuni.edu"}
    };

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
            if (allUsers.get(i).getUserId().equalsIgnoreCase(userId)) return i;
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
