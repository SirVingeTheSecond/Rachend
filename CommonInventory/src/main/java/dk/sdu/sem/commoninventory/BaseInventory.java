package dk.sdu.sem.commonInventory;

import dk.sdu.sem.commonsystem.IComponent;

import java.util.ArrayList;

public abstract class BaseInventory<T> implements IComponent {

	private ArrayList<T> itemInventory = new ArrayList<>();

	/**
	 * checks if the player has a certain active item
	 * @param item
	 * @return The given item, if the player has it. Else returns null
	 */
	public T getItemInInventory(T item) {

		for (T i : itemInventory) {
			if (i.equals(item)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Adds an item to the players inventory
	 * @param item
	 */
	public void addItem(T item) {
		itemInventory.add(item);
	}

	/**
	 * Removes an item from the players inventory
	 * @param item
	 */
	public void removeItem(T item) {
		itemInventory.remove(item);
	}

	/**
	 * Removes an item from the players inventory
	 * @param index
	 */
	public void removeItem(int index) {
		itemInventory.remove(index);
	}

	/**
	 * Clears the players inventory
	 */
	public void removeAllItems() {
		itemInventory.clear();
	}
}
