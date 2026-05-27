package main;

import scheduling.TimeSlot;
import scheduling.Resource;
import users.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DataManager {
    private static final String FILE_NAME = "data/appointments_data.txt";
    private static final String USERS_FILE_NAME = "data/users_data.csv";
    private static final String RESOURCES_FILE_NAME = "data/resources_data.csv";

    public static void saveState(List<TimeSlot> slots) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write("timeRange|resourceName|resourceCapacity|confirmedStudentIds|waitlistStudentIds|creatorId|creatorName|status|title|slotCapacity");
            writer.newLine();
            for (TimeSlot slot : slots) {
                writer.write(clean(slot.getTimeRange()) + "|" + clean(slot.getResource().getName()) + "|"
                        + slot.getResource().getCapacity() + "|" + joinStudentIds(slot.getConfirmedStudents())
                        + "|" + joinStudentIds(slot.getWaitlist()) + "|"
                        + clean(slot.getCreatorId()) + "|" + clean(slot.getCreatorName()) + "|"
                        + clean(slot.getStatus()) + "|" + clean(slot.getTitle()) + "|" + slot.getSlotCapacity());
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
                        TimeSlot slot;
                        if (parts.length >= 10) {
                            slot = new TimeSlot(parts[0], res, parts[5], parts[6], parts[7], parts[8], Integer.parseInt(parts[9]));
                        } else {
                            slot = new TimeSlot(parts[0], res);
                        }
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

    public static void saveNotifications(List<scheduling.Notification> notifications) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("data/notifications_data.txt"))) {
            pw.println("userId|message|timestamp");
            for (scheduling.Notification notif : notifications) {
                pw.println(notif.getUserId() + "|" + notif.getMessage() + "|" + notif.getTime().toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving notifications: " + e.getMessage());
        }
    }

    public static List<scheduling.Notification> loadNotifications() {
        List<scheduling.Notification> list = new ArrayList<>();
        File file = new File("data/notifications_data.txt");
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    list.add(new scheduling.Notification(parts[0], parts[1], java.time.LocalDateTime.parse(parts[2])));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading notifications: " + e.getMessage());
        }
        return list;
    }
}
