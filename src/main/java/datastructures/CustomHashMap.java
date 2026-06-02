package datastructures;

// A generic Hash Map using Separate Chaining for collision resolution
public class CustomHashMap<K, V> {
	private static final int INITIAL_CAPACITY = 16;
	private Entry<K, V>[] table;
	private int size;

	@SuppressWarnings("unchecked")
	public CustomHashMap() {
		table = new Entry[INITIAL_CAPACITY];
		size = 0;
	}

	// Inner class for the linked list nodes
	private static class Entry<K, V> {
		K key;
		V value;
		Entry<K, V> next;

		public Entry(K key, V value, Entry<K, V> next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

	private int getBucketIndex(K key) {
		return Math.abs(key.hashCode()) % table.length;
	}

	// Insertion: O(n)
	public void put(K key, V value) {
		// STEP 1: Compute hash code to find the bucket index
		int index = getBucketIndex(key);
		Entry<K, V> head = table[index];

		// STEP 2: Check if key already exists, update value if it does
		Entry<K, V> current = head;
		while (current != null) {
			if (current.key.equals(key)) {
				current.value = value;
				return;
			}
			current = current.next;
		}

		// STEP 3: Insert new entry at the head of the chain (collision resolution)
		table[index] = new Entry<>(key, value, head);
		size++;
	}

	// Lookup: O(n)
	public V get(K key) {
		// STEP 1: Find the bucket corresponding to the hash code
		int index = getBucketIndex(key);
		Entry<K, V> current = table[index];

		// STEP 2: Traverse the linked list in the bucket
		while (current != null) {
			if (current.key.equals(key)) {
				return current.value;
			}
			current = current.next;
		}
		return null; // Key not found
	}

	public boolean containsKey(K key) {
		return get(key) != null;
	}

	public int size() {
		return size;
	}
}