package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Data component that defines a pickup item's properties.
 * This contains information about the item that can be collected.
 */
public class PickupComponent implements IComponent {
	private final String itemType;
	private final float value;
	private boolean consumed = false;

	/**
	 * Creates a pickup component for item collection.
	 *
	 * @param itemType The type of item (e.g., "health", "coin", "weapon")
	 * @param value The value of the item
	 */
	public PickupComponent(String itemType, float value) {
		this.itemType = itemType;
		this.value = value;
	}

	/**
	 * Gets the type of this item.
	 * @return The item type identifier
	 */
	public String getItemType() {
		return itemType;
	}

	/**
	 * Gets the value of this item.
	 * @return The numerical value of the item
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Checks if this item has been consumed.
	 * @return True if the item has been picked up and consumed
	 */
	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * Marks the item as consumed.
	 * @param consumed The new consumed state
	 */
	public void setConsumed(boolean consumed) {
		this.consumed = consumed;
	}
}