package scheduling;

public class Resource {
	private String name;
	private int capacity;

	public Resource(String name, int capacity) {
		this.name = name;
		this.capacity = capacity;
	}

	public String getName() {
		return name;
	}

	public int getCapacity() {
		return capacity;
	}
}