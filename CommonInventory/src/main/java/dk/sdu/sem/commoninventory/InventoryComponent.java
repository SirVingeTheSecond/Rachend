package dk.sdu.sem.commoninventory;

import dk.sdu.sem.commonsystem.IComponent;

import java.util.HashMap;
import java.util.Map;

public class InventoryComponent implements IComponent {
	private final Map<String, Integer> items = new HashMap<>();
	private final int maxCapacity;

	public InventoryComponent() {
		this(20);
	}

	public InventoryComponent(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public boolean addItem(String type, int amount) {
		if (getTotalItemCount() + amount > maxCapacity) {
			return false;
		}

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

	public int getTotalItemCount() {
		return items.values().stream().mapToInt(Integer::intValue).sum();
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}
}