package users;

public abstract class User {
	protected String userId;
	protected String name;
	protected String email;
	protected String password;
	protected boolean isApproved = false; // Default to false for new signups

	public User(String userId, String name, String email, String password) {
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public boolean isApproved() {
		return isApproved;
	}

	public void setApproved(boolean approved) {
		this.isApproved = approved;
	}
}