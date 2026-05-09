package users;

// The base template for all people
public abstract class User {
	protected String userId;
	protected String name;

	public User(String userId, String name) {
		this.userId = userId;
		this.name = name;
	}

	public String getName() {
		return name;
	}
}