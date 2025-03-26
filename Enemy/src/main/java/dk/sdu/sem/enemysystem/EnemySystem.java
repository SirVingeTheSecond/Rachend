package dk.sdu.sem.enemysystem;


import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.playersystem.PlayerNode;


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
		PlayerNode yes = NodeManager.active().getNodes(PlayerNode.class).iterator().next();
		// we assume there preexists 1 player entity on the active scene.
		Vector2D playerLocation =
			yes.getEntity().getComponent(TransformComponent.class).getPosition();

		for (EnemyNode node : enemyNodes) {
			moveTowards(node.physics,node.enemy,playerLocation.getX(),
				playerLocation.getY() );

		}
	}


	private void moveTowards(PhysicsComponent physicsComponent,
							 EnemyComponent enemyComponent, float xMove,
							 float yMove) {
		float moveSpeed = enemyComponent.getMoveSpeed();

		// Create movement vector
		Vector2D moveVector = new Vector2D(xMove, yMove)
			.normalize()
			.scale(moveSpeed * (float)Time.getDeltaTime());
		Vector2D velocity = physicsComponent.getVelocity();
		Vector2D newVelocity = velocity.add(moveVector);

		physicsComponent.setVelocity(newVelocity);
	}
}
