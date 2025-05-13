package dk.sdu.sem.dashability;

import dk.sdu.sem.commonparticle.ParticleEmitterComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IEntityLifecycleListener;
import dk.sdu.sem.player.PlayerComponent;

public class DashAbilityLifetime implements IEntityLifecycleListener {
	@Override
	public void onEntityRemoved(Entity entity) {
		//Empty
	}

	@Override
	public void onEntityAdded(Entity entity) {
		if (!entity.hasComponent(PlayerComponent.class))
			return;

		if (entity.hasComponent(DashAbilityComponent.class))
			return;

		DashAbilityComponent dashComponent = new DashAbilityComponent();
		dashComponent.setFadeDelay(0.3);
		dashComponent.setFadeDuration(0.2);
		entity.addComponent(dashComponent);

		entity.addComponent(new ParticleEmitterComponent(100));
	}
}
