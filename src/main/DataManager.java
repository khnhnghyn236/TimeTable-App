package main;

import scheduling.TimeSlot;
import scheduling.Resource;
import users.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String FILE_NAME = "appointments_data.txt";
    private static final String USERS_FILE_NAME = "users_data.csv";

    public static void saveState(List<TimeSlot> slots) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (TimeSlot slot : slots) {
                writer.write(slot.getTimeRange() + "," + slot.getResource().getName() + "," + slot.getResource().getCapacity());
                writer.newLine();
            }
            System.out.println("Appointment data saved to " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("Error saving appointment data: " + e.getMessage());
        }
    }

    public static List<TimeSlot> loadState() {
        List<TimeSlot> loadedSlots = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return loadedSlots;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    Resource res = new Resource(parts[1], Integer.parseInt(parts[2]));
                    loadedSlots.add(new TimeSlot(parts[0], res));
                }
            }
            System.out.println("Appointment data loaded.");
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading appointment data: " + e.getMessage());
        }
        return loadedSlots;
    }

    public static void saveUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE_NAME))) {
            writer.write("role|userId|name|email|passwordHash|approved");
            writer.newLine();
            for (User user : users) {
                writer.write(getRole(user) + "|" + clean(user.getUserId()) + "|" + clean(user.getName()) + "|" +
                        clean(user.getEmail()) + "|" + user.getPasswordHash() + "|" + user.isApproved());
                writer.newLine();
            }
            System.out.println("User data saved to " + USERS_FILE_NAME);
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(USERS_FILE_NAME);
        if (!file.exists()) return users;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length != 6) continue;
                String role = parts[0], userId = parts[1], name = parts[2], email = parts[3], passwordHash = parts[4];
                boolean approved = Boolean.parseBoolean(parts[5]);
                User user = null;
                if (role.equals("ADMIN")) user = new Administrator(userId, name, email, passwordHash, true);
                else if (role.equals("STUDENT")) user = new Student(userId, name, email, passwordHash, true);
                else if (role.equals("STAFF")) user = new AcademicStaff(userId, name, email, passwordHash, true);
                if (user != null) {
                    user.setApproved(approved);
                    users.add(user);
                }
            }
            System.out.println("User data loaded.");
        } catch (IOException e) {
            System.out.println("Error loading user data: " + e.getMessage());
        }
        return users;
    }

    private static String getRole(User user) {
        if (user instanceof Administrator) return "ADMIN";
        if (user instanceof AcademicStaff) return "STAFF";
        return "STUDENT";
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace("|", " ").trim();
    }
}
