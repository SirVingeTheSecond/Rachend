package dk.sdu.sem.enemysystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonpathfinding.IPathfindingTargetProvider;

import java.util.Optional;

/**
 * Chooses between live player position (FOLLOWING)
 * or last known position (SEARCHING), or no target (IDLE).
 */
public class StateBasedTargetProvider implements IPathfindingTargetProvider {
	private final Entity enemyEntity;
	private final Entity playerEntity;

	public StateBasedTargetProvider(Entity enemyEntity, Entity playerEntity) {
		this.enemyEntity = enemyEntity;
		this.playerEntity = playerEntity;
	}

	@Override
	public Optional<Vector2D> getTarget() {
		LastKnownPositionComponent lastKnown =
			enemyEntity.getComponent(LastKnownPositionComponent.class);

		// always fetch current player position
		Vector2D playerPos = playerEntity
			.getComponent(TransformComponent.class)
			.getPosition();

		if (lastKnown == null || lastKnown.getState() == EnemyState.FOLLOWING) {
			return Optional.of(playerPos);
		}

		if (lastKnown.getState() == EnemyState.SEARCHING) {
			return Optional.of(lastKnown.getLastKnownPosition());
		}

		// IDLE
		return Optional.empty();
	}
}