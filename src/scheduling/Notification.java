package scheduling;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private String userId;
    private String message;
    private LocalDateTime time;

    public Notification(String userId, String message) {
        this.userId = userId;
        this.message = message;
        this.time = LocalDateTime.now();
    }

    public Notification(String userId, String message, LocalDateTime time) {
        this.userId = userId;
        this.message = message;
        this.time = time;
    }

    public String getUserId() { return userId; }
    public String getMessage() { return message; }
    public LocalDateTime getTime() { return time; }
    
    public String getFormattedTime() {
        return time.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm"));
    }
}
