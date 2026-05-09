package users;

import scheduling.Resource;

public class Administrator extends User {
	public Administrator(String userId, String name, String email, String password) {
		super(userId, name, email, password);
		this.isApproved = true; // Admins are always approved by default
	}

	public Resource createResource(String name, int capacity) {
		System.out.println("Admin " + this.name + " created resource: " + name + " (Capacity: " + capacity + ")");
		return new Resource(name, capacity);
	}
}