package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.Entity;

public interface IItem {

	ItemType getType();

	String getName();

	String getSpriteName();

	boolean applyEffect(Entity entity);

	IItem createInstance();
}
