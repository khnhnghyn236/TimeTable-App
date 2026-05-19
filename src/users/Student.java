package users;

public class Student extends User {
    public Student(String userId, String name, String email, String password) {
        super(userId, name, email, password);
    }
    public Student(String userId, String name, String email, String passwordHash, boolean alreadyHashed) {
        super(userId, name, email, passwordHash, alreadyHashed);
    }
}
