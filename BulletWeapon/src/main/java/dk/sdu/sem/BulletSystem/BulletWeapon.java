package dk.sdu.sem.BulletSystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonsystem.Node;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IWeapon;
import dk.sdu.sem.gamesystem.components.TransformComponent;

import java.util.Set;

public class BulletWeaponNode extends Node implements IWeapon {


	// spawn a BulletNode, which can be observed BulletSystem
	@Override
	public void activateWeapon(Node activator) {
		BulletNode bulletNode = new BulletNode();
		bulletNode.transformComponent.setPosition(new Vector2D(1, 100.0f));
	}

	@Override
	public Set<Class<? extends IComponent>> getRequiredComponents() {
		return Set.of();
	}
}
