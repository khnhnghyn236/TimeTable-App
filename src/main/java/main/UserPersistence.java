package main;

import java.io.*;

public class UserPersistence {
    private static final String REMEMBER_FILE = "data/remembered_user.txt";

    public static void saveRememberedId(String userId) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REMEMBER_FILE))) {
            writer.write(userId == null ? "" : userId.trim());
        } catch (IOException e) {
            System.out.println("Error saving remembered ID: " + e.getMessage());
        }
    }

    public static String loadRememberedId() {
        File file = new File(REMEMBER_FILE);
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String id = reader.readLine();
            return (id == null || id.trim().isEmpty()) ? null : id.trim();
        } catch (IOException e) {
            System.out.println("Error loading remembered ID: " + e.getMessage());
            return null;
        }
    }

    public static void clearRememberedId() {
        File file = new File(REMEMBER_FILE);
        if (file.exists()) file.delete();
    }
}
