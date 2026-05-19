package users;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public abstract class User {
    protected String userId;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected boolean isApproved = false;

    public User(String userId, String name, String email, String rawPassword) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = hashPassword(rawPassword);
    }

    public User(String userId, String name, String email, String passwordHash, boolean alreadyHashed) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = alreadyHashed ? passwordHash : hashPassword(passwordHash);
    }

    public String getUserId() { return userId; }
    public String getId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPassword() { return passwordHash; }
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { this.isApproved = approved; }

    public boolean verifyPassword(String rawPassword) {
        return passwordHash.equals(hashPassword(rawPassword));
    }

    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
