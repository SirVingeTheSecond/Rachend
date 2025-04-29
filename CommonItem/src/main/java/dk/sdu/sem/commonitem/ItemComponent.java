package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component representing any collectible item in the game.
 */
public class ItemComponent implements IComponent {
	private final ItemType type;
	private final String name;

	public ItemComponent(ItemType type, String name) {
		this.type = type;
		this.name = name;
	}

	public ItemType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "ItemComponent{type='" + type + "', name=" + name + '}';
	}
}