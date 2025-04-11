package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.PhysicsLayer;
import dk.sdu.sem.collision.components.ColliderComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweaponsystem.IWeaponSPI;
import dk.sdu.sem.commonweaponsystem.WeaponComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;

public class MeleeWeapon implements IWeaponSPI {
	// these attributes are needed to allow changing the collider instance of
	// the weapon location
	// when the attacker moves, avoiding lag spikes when an object is
	// instanciated
	// these should be from weaponcomponent
	Vector2D position;
	float radius;
	Entity colliderEntity;


	//  TODO how to get a collider on this shape ?
//	RectangleShape hitregShape = new RectangleShape(position, width, radius);

//	public RectangleShape getHitregShape() {
//		return hitregShape;
//	}

	/**
 * @param direction  Direction of the attack check.
 * @param activator  The entity which activates the weapon.
 */
	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		this.position = activator.getComponent(TransformComponent.class).getPosition();
		// Could use another shape than circle shape
		this.radius =
			activator.getComponent(WeaponComponent.class).getAttackSize()*direction.normalize().y();

		// instantiation at runtime is bad, at it induces small lagspike
		// make an ColliderNode.class
		if (colliderEntity == null) {
			colliderEntity = new Entity();
			// rotation is idk,
			// loops with colliding with itself
			colliderEntity.addComponent(new TransformComponent(position,0));
			// direction normalize
			colliderEntity.addComponent(new ColliderComponent(colliderEntity,
				direction.normalize(),
				// this could be set based on if the has enemy or player
				// component as then the weapon could hit other projectiles.
				radius,true, PhysicsLayer.PROJECTILE));
			// slightly offsat position to hopefully prevent infinite collisions
			ColliderComponent colliderComponent =
				colliderEntity.getComponent(ColliderComponent.class);
			colliderEntity.addComponent(new MeleeHitTriggerListener(activator));
		}
	}
// run animation
//	private Vector2D getAttackBoxRelativePosition (float normalizedDirection){
//		// direction is normalized
//		// this works for both y and x values, as the function is just
//		// invocted 2 times.
//		if (direction.normalize().x()>0){
//
//		}
//		else if (direction.normalize().x()<0){
//
//		}
//		// then it is 0 or horzontal
//		else {
//
//		}
//		if (direction.normalize().x()=0){
//
//		}
//
//
//
//
//
//	}
}
