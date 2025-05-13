package dk.sdu.sem.commoninventory;

import dk.sdu.sem.commonsystem.IComponent;

import java.util.HashMap;
import java.util.Map;

public class InventoryComponent implements IComponent {
	private final Map<String, Integer> items = new HashMap<>();

	public InventoryComponent() {
	}

	public boolean addItem(String type, int amount) {
		items.put(type, items.getOrDefault(type, 0) + amount);
		return true;
	}

	public int getItemCount(String type) {
		return items.getOrDefault(type, 0);
	}

	public Map<String, Integer> getItems() {
		return new HashMap<>(items);
	}

	public boolean removeItem(String type, int amount) {
		int current = items.getOrDefault(type, 0);

		// Check if we have enough
		if (current < amount) {
			return false;
		}

		// Update or remove
		int newAmount = current - amount;
		if (newAmount > 0) {
			items.put(type, newAmount);
		} else {
			items.remove(type);
		}

		return true;
	}
}