package users;

public class AcademicStaff extends User {
    public AcademicStaff(String userId, String name, String email, String password) {
        super(userId, name, email, password);
    }
    public AcademicStaff(String userId, String name, String email, String passwordHash, boolean alreadyHashed) {
        super(userId, name, email, passwordHash, alreadyHashed);
    }
}
