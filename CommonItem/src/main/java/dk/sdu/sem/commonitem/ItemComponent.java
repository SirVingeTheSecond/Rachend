package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component representing any collectible item in the game.
 */
public class ItemComponent implements IComponent {
	private final String type;
	private final int value;

	public ItemComponent(String type, int value) {
		this.type = type;
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ItemComponent{type='" + type + "', value=" + value + '}';
	}
}