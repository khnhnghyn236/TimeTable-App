package main;

import scheduling.TimeSlot;
import scheduling.Resource;
import users.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DataManager {
    private static final String FILE_NAME = "appointments_data.txt";
    private static final String USERS_FILE_NAME = "users_data.csv";
    private static final String RESOURCES_FILE_NAME = "resources_data.csv";

    public static void saveState(List<TimeSlot> slots) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write("timeRange|resourceName|resourceCapacity|confirmedStudentIds|waitlistStudentIds");
            writer.newLine();
            for (TimeSlot slot : slots) {
                writer.write(clean(slot.getTimeRange()) + "|" + clean(slot.getResource().getName()) + "|"
                        + slot.getResource().getCapacity() + "|" + joinStudentIds(slot.getConfirmedStudents())
                        + "|" + joinStudentIds(slot.getWaitlist()));
                writer.newLine();
            }
            System.out.println("Appointment data saved to " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("Error saving appointment data: " + e.getMessage());
        }
    }

    public static List<TimeSlot> loadState() {
        return loadState(new ArrayList<User>());
    }

    public static List<TimeSlot> loadState(List<User> users) {
        List<TimeSlot> loadedSlots = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return loadedSlots;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (firstLine && line.startsWith("timeRange|")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                // New professional format
                if (line.contains("|")) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 3) {
                        Resource res = new Resource(parts[1], Integer.parseInt(parts[2]));
                        TimeSlot slot = new TimeSlot(parts[0], res);
                        if (parts.length >= 4) loadStudentsIntoSlot(parts[3], users, slot, true);
                        if (parts.length >= 5) loadStudentsIntoSlot(parts[4], users, slot, false);
                        loadedSlots.add(slot);
                    }
                } else {
                    // Backward compatible old format: timeRange,resourceName,capacity
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        Resource res = new Resource(parts[1], Integer.parseInt(parts[2]));
                        loadedSlots.add(new TimeSlot(parts[0], res));
                    }
                }
            }
            System.out.println("Appointment data loaded.");
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading appointment data: " + e.getMessage());
        }
        return loadedSlots;
    }

    public static void saveResources(List<Resource> resources) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESOURCES_FILE_NAME))) {
            writer.write("name|capacity");
            writer.newLine();
            for (Resource resource : resources) {
                writer.write(clean(resource.getName()) + "|" + resource.getCapacity());
                writer.newLine();
            }
            System.out.println("Resource data saved to " + RESOURCES_FILE_NAME);
        } catch (IOException e) {
            System.out.println("Error saving resource data: " + e.getMessage());
        }
    }

    public static List<Resource> loadResources() {
        List<Resource> resources = new ArrayList<>();
        File file = new File(RESOURCES_FILE_NAME);
        if (!file.exists()) return resources;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length == 2) {
                    resources.add(new Resource(parts[0], Integer.parseInt(parts[1])));
                }
            }
            System.out.println("Resource data loaded.");
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading resource data: " + e.getMessage());
        }
        return resources;
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
            String line = reader.readLine();
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

    private static void loadStudentsIntoSlot(String ids, List<User> users, TimeSlot slot, boolean confirmed) {
        if (ids == null || ids.trim().isEmpty()) return;
        for (String id : ids.split(";")) {
            Student student = findStudentById(users, id.trim());
            if (student != null) {
                if (confirmed) slot.addConfirmedStudent(student);
                else slot.addWaitlistedStudent(student);
            }
        }
    }

    private static Student findStudentById(List<User> users, String id) {
        for (User user : users) {
            if (user instanceof Student && user.getUserId().equalsIgnoreCase(id)) return (Student) user;
        }
        return null;
    }

    private static String joinStudentIds(List<Student> students) {
        List<String> ids = new ArrayList<>();
        for (Student student : students) ids.add(student.getUserId());
        return String.join(";", ids);
    }

    private static String joinStudentIds(Queue<Student> students) {
        List<String> ids = new ArrayList<>();
        for (Student student : students) ids.add(student.getUserId());
        return String.join(";", ids);
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
