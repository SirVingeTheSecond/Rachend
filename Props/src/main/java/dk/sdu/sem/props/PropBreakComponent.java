package dk.sdu.sem.props;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.events.*;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonweapon.BulletComponent;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.function.Consumer;

public class PropBreakComponent implements IComponent, ITriggerListener {
	private boolean broken = false;
	private SpriteReference brokenSprite;

	public PropBreakComponent(Entity entity, SpriteReference brokenSprite) {
		this.brokenSprite = brokenSprite;
	}


	@Override
	public void onTriggerEnter(TriggerEnterEvent event) {
		if (broken)
			return;

		if (!event.getOther().hasComponent(BulletComponent.class))
			return;

		event.getEntity()
			.getComponent(SpriteRendererComponent.class)
			.setSprite(brokenSprite);

		event.getEntity().removeComponent(CircleColliderComponent.class);

		broken = true;
	}

	@Override
	public void onTriggerStay(TriggerStayEvent event) {

	}

	@Override
	public void onTriggerExit(TriggerExitEvent event) {

	}
}
