package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.IComponent;

/**
 * Component representing any collectible item in the game.
 */

// ToDO change itemtype to IItem and give it an item, change constructors and getters.
public class ItemComponent implements IComponent {
	private final IItem item;
	private final ItemType type;
	private final String name;
	private boolean collected = false;

	public ItemComponent(IItem item) {
		this.item = item;
		this.type = item.getType();
		this.name = item.getName();

	}

	public IItem getItem() {
		return item;
	}

	public ItemType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public boolean isCollected() {
		return collected;
	}

	public void setCollected(boolean collected) {
		this.collected = collected;
	}

	@Override
	public String toString() {
		return "ItemComponent{type='" + type + "', name=" + name + '}';
	}
}