package datastructures;

import java.util.ArrayList;
import java.util.List;

// A generic Binary Search Tree for any Comparable objects
public class CustomBST<T extends Comparable<T>> {
	private Node root;

	private class Node {
		T data;
		Node left, right;

		public Node(T data) {
			this.data = data;
			left = right = null;
		}
	}

	// Insertion: O(log n)
	public void insert(T data) {
		root = insertRec(root, data);
	}

	private Node insertRec(Node root, T data) {
		// STEP 1: If the tree is empty, return a new node
		if (root == null) {
			root = new Node(data);
			return root;
		}
		// STEP 2: Otherwise, recur down the tree
		if (data.compareTo(root.data) < 0) {
			root.left = insertRec(root.left, data);
		} else if (data.compareTo(root.data) > 0) {
			root.right = insertRec(root.right, data);
		}
		return root;
	}

	// Retrieves elements in sorted order
	public List<T> getSortedList() {
		List<T> sortedList = new ArrayList<>();
		inOrderRec(root, sortedList);
		return sortedList;
	}

	private void inOrderRec(Node root, List<T> list) {
		// STEP 1: Traverse left, visit node, traverse right (In-Order)
		if (root != null) {
			inOrderRec(root.left, list);
			list.add(root.data);
			inOrderRec(root.right, list);
		}
	}

	public void clear() {
		root = null;
	}
}