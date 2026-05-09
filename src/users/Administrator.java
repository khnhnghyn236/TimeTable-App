package users;

import scheduling.Resource;

public class Administrator extends User {

	public Administrator(String userId, String name) {
		super(userId, name);
	}

	// Admin creates a resource with a specific capacity
	public Resource createResource(String name, int capacity) {
		System.out.println("Admin " + this.name + " created resource: " + name + " (Capacity: " + capacity + ")");
		return new Resource(name, capacity);
	}
}