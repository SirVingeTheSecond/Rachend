package dk.sdu.sem.commonitem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;

public interface IItemFactory {
	Entity create();
	Entity createCoin(Vector2D position);
	Entity createHealthPotion(Vector2D position);
}
