package main;

import scheduling.TimeSlot;
import scheduling.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
	private static final String FILE_NAME = "appointments_data.txt";

	// Saves the current systemTimeSlots to a text file
	public static void saveState(List<TimeSlot> slots) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
			for (TimeSlot slot : slots) {
				// Save format: TimeRange,ResourceName,ResourceCapacity
				writer.write(slot.getTimeRange() + "," + slot.getResource().getName() + ","
						+ slot.getResource().getCapacity());
				writer.newLine();
			}
			System.out.println("Data successfully saved to " + FILE_NAME);
		} catch (IOException e) {
			System.out.println("Error saving data: " + e.getMessage());
		}
	}

	// Loads the systemTimeSlots from the text file on startup
	public static List<TimeSlot> loadState() {
		List<TimeSlot> loadedSlots = new ArrayList<>();
		File file = new File(FILE_NAME);

		if (!file.exists())
			return loadedSlots; // Return empty if first time running

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length == 3) {
					Resource res = new Resource(parts[1], Integer.parseInt(parts[2]));
					loadedSlots.add(new TimeSlot(parts[0], res));
				}
			}
			System.out.println("Data successfully loaded.");
		} catch (IOException | NumberFormatException e) {
			System.out.println("Error loading data: " + e.getMessage());
		}
		return loadedSlots;
	}
}