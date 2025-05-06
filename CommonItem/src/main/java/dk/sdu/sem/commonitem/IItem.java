package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.Entity;

public interface IItem {

	ItemType getType();

	String getName();

	void applyEffect(Entity entity);

	IItem createInstance();
}
