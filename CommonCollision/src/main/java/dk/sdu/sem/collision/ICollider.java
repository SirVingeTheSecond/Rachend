package dk.sdu.sem.collision;

import javax.swing.text.html.parser.Entity;

public interface ICollider {
	/**
	 * Returns the entity associated with this collider.
	 */
	Entity getEntity();

	/**
	 * Returns the collision shape for this collider.
	 */
	ICollisionShape getCollisionShape();
}
