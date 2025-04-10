package dk.sdu.sem.enemysystem;


import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.commonsystem.NodeManager;


import java.util.Optional;
import java.util.Set;

// updates enemy state
public class EnemySystem implements IUpdate {

	@Override
	public void update() {

		// get all Enemies on active scene
		Set<EnemyNode> enemyNodes =
			NodeManager.active().getNodes(EnemyNode.class);
		if (enemyNodes.isEmpty()) {
			return;
		}
		// temporary code to get the location of the player
		PlayerTargetNode playerNode = NodeManager.active().getNodes(PlayerTargetNode.class).stream().findFirst().orElse(null);
		if (playerNode == null)
			return;

		// we assume there preexists 1 player entity on the active scene.
		Vector2D playerLocationVector =
			playerNode.getEntity().getComponent(TransformComponent.class).getPosition();

		for (EnemyNode node : enemyNodes) {
			node.pathfinding.current().ifPresent(route -> {
				route = toWorldPosition(route).add(new Vector2D(0.5f, 0.5f));

//				if (Vector2D.euclidean_distance(route, playerLocationVector) < (24 * 2f)) {
//					node.pathfinding.advance();
//				}
//
				Vector2D direction = route.subtract(node.transform.getPosition()).normalize();
				moveTowards(node.physics, node.enemy, direction);
				node.weapon.getWeapon().activateWeapon(node.getEntity(),direction);
			});
		}
	}

	private static Vector2D toWorldPosition(Vector2D position) {
		return position.scale((float) GameConstants.TILE_SIZE);
	}

	private void moveTowards(PhysicsComponent physicsComponent,
							 EnemyComponent enemyComponent,
							 Vector2D direction) {
		float moveSpeed = enemyComponent.getMoveSpeed();

		// Create movement vector
		Vector2D moveVector = direction
			.scale(moveSpeed * (float)Time.getDeltaTime());
		Vector2D velocity = physicsComponent.getVelocity();
		Vector2D newVelocity = velocity.add(moveVector);

		physicsComponent.setVelocity(newVelocity);
	}
}
