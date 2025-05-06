package dk.sdu.sem.dashability;

import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.Optional;
import java.util.Set;

public class DashAbilitySystem implements IUpdate {
	private static final Logging LOGGER = Logging.createLogger("DashAbilitySystem", LoggingLevel.DEBUG);

	@Override
	public void update() {
		Set<DashAbilityNode> nodes = NodeManager.active().getNodes(DashAbilityNode.class);

		nodes.forEach(node -> {
			PhysicsComponent physics = node.physics;
			DashAbilityComponent dash = node.dash;
			Optional<AnimatorComponent> animator = Optional.ofNullable(node.getEntity().getComponent(AnimatorComponent.class));

			Vector2D move = Input.getMove();

			dash.update(Time.getDeltaTime());
			if (Input.getKeyDown(Key.SPACE) && dash.isOnCooldown() == false) {
				Vector2D velocity = move.scale(dash.velocityScale);
				dash.use();
				animator.ifPresent(anim -> anim.setParameter("isDashing", true));
				physics.addImpulse(velocity);
			}
		});
	}
}
